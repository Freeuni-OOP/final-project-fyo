package com.fyo.match;

import com.fyo.chat.ChatService;
import com.fyo.chat.dto.ConversationResponse;
import com.fyo.domain.Match;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchListing;
import com.fyo.domain.MatchListingResponse;
import com.fyo.domain.MatchListingResponseStatus;
import com.fyo.domain.MatchListingStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.match.dto.AcceptListingResult;
import com.fyo.match.dto.CreateMatchListingRequest;
import com.fyo.match.dto.ListingResponseItem;
import com.fyo.match.dto.MatchListingItem;
import com.fyo.match.dto.MatchParticipantResponse;
import com.fyo.match.dto.RespondToListingRequest;
import com.fyo.match.dto.SportResponse;
import com.fyo.repository.MatchListingRepository;
import com.fyo.repository.MatchListingResponseRepository;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Orchestrates the listing lifecycle: post → respond → accept. Accepting a
 * response is the moment a {@link Match} is born; the same transaction fills
 * the listing, declines every other pending response, and auto-creates the
 * match conversation so both sides can settle time, place, and rules.
 */
@Service
public class MatchListingService {

    private final MatchListingRepository listingRepository;
    private final MatchListingResponseRepository responseRepository;
    private final MatchRepository matchRepository;
    private final SportRepository sportRepository;
    private final TeamRepository teamRepository;
    private final ChatService chatService;

    public MatchListingService(
            MatchListingRepository listingRepository,
            MatchListingResponseRepository responseRepository,
            MatchRepository matchRepository,
            SportRepository sportRepository,
            TeamRepository teamRepository,
            ChatService chatService
    ) {
        this.listingRepository = listingRepository;
        this.responseRepository = responseRepository;
        this.matchRepository = matchRepository;
        this.sportRepository = sportRepository;
        this.teamRepository = teamRepository;
        this.chatService = chatService;
    }

    @Transactional
    public MatchListingItem createListing(User currentUser, CreateMatchListingRequest request) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found"));

        MatchListing listing;
        if (request.teamId() == null) {
            listing = MatchListing.postedByUser(sport, currentUser, request.location(), request.proposedDatetime());
        } else {
            Team team = requireCaptainedTeam(currentUser, request.teamId());
            listing = MatchListing.postedByTeam(sport, team, request.location(), request.proposedDatetime());
        }

        return toListingItem(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public List<MatchListingItem> getOpenListings(Long sportId) {
        List<MatchListing> listings = sportId == null
                ? listingRepository.findByStatus(MatchListingStatus.OPEN)
                : listingRepository.findBySportIdAndStatus(sportId, MatchListingStatus.OPEN);
        return listings.stream().map(this::toListingItem).toList();
    }

    @Transactional
    public ListingResponseItem respondToListing(User currentUser, Long listingId, RespondToListingRequest request) {
        MatchListing listing = requireListing(listingId);
        if (listing.getStatus() != MatchListingStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This listing is no longer open");
        }

        MatchListingResponse response;
        if (listing.getFormat() == MatchFormat.ONE_VS_ONE) {
            if (request.teamId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A 1v1 listing takes individual responses, not team ones");
            }
            if (listing.isPostedBy(currentUser.getId(), null)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot respond to your own listing");
            }
            if (responseRepository.existsByListingIdAndResponderUserId(listingId, currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already responded to this listing");
            }
            response = MatchListingResponse.fromUser(listing, currentUser);
        } else {
            if (request.teamId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A team listing needs a responding team");
            }
            Team team = requireCaptainedTeam(currentUser, request.teamId());
            if (listing.isPostedBy(null, team.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A team cannot respond to its own listing");
            }
            if (responseRepository.existsByListingIdAndResponderTeamId(listingId, team.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This team already responded to this listing");
            }
            response = MatchListingResponse.fromTeam(listing, team);
        }

        return toResponseItem(responseRepository.save(response));
    }

    @Transactional(readOnly = true)
    public List<ListingResponseItem> getPendingResponses(User currentUser, Long listingId) {
        MatchListing listing = requireListing(listingId);
        requirePoster(currentUser, listing);
        return responseRepository.findByListingIdAndStatus(listingId, MatchListingResponseStatus.PENDING)
                .stream()
                .map(this::toResponseItem)
                .toList();
    }

    /**
     * Poster accepts one response: the match is created (poster = home,
     * responder = away), the listing is filled, every other pending response
     * is declined, and the match conversation is created — all atomically.
     */
    @Transactional
    public AcceptListingResult acceptResponse(User currentUser, Long listingId, Long responseId) {
        MatchListing listing = requireListing(listingId);
        requirePoster(currentUser, listing);
        if (listing.getStatus() != MatchListingStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This listing is no longer open");
        }

        MatchListingResponse accepted = responseRepository.findByIdAndListingId(responseId, listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found for this listing"));
        if (accepted.getStatus() != MatchListingResponseStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a pending response can be accepted");
        }

        accepted.accept();

        Match match = matchRepository.save(buildMatch(listing, accepted));
        listing.fill(match);
        listingRepository.save(listing);

        responseRepository.findByListingIdAndStatus(listingId, MatchListingResponseStatus.PENDING)
                .forEach(MatchListingResponse::decline);

        ConversationResponse conversation = chatService.createMatchConversation(match);

        return new AcceptListingResult(listing.getId(), match.getId(), conversation.id());
    }

    @Transactional
    public ListingResponseItem declineResponse(User currentUser, Long listingId, Long responseId) {
        MatchListing listing = requireListing(listingId);
        requirePoster(currentUser, listing);

        MatchListingResponse response = responseRepository.findByIdAndListingId(responseId, listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found for this listing"));
        if (response.getStatus() != MatchListingResponseStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only a pending response can be declined");
        }

        response.decline();
        return toResponseItem(response);
    }

    private Match buildMatch(MatchListing listing, MatchListingResponse accepted) {
        if (listing.getFormat() == MatchFormat.ONE_VS_ONE) {
            return Match.oneVsOne(
                    listing.getSport(),
                    listing.getPostedByUser(),
                    accepted.getResponderUser(),
                    listing.getLocation(),
                    listing.getProposedDatetime()
            );
        }
        return Match.teamVsTeam(
                listing.getSport(),
                listing.getPostedByTeam(),
                accepted.getResponderTeam(),
                listing.getLocation(),
                listing.getProposedDatetime()
        );
    }

    private MatchListing requireListing(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
    }

    /** 1v1 listings are owned by their poster; team listings by the posting team's captain. */
    private void requirePoster(User currentUser, MatchListing listing) {
        if (listing.getFormat() == MatchFormat.ONE_VS_ONE) {
            if (!listing.isPostedBy(currentUser.getId(), null)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the listing poster can do this");
            }
            return;
        }
        Team postingTeam = listing.getPostedByTeam();
        if (postingTeam == null || !currentUser.getId().equals(postingTeam.getCaptain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the posting team's captain can do this");
        }
    }

    private Team requireCaptainedTeam(User currentUser, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        if (!currentUser.getId().equals(team.getCaptain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team captain can act for a team");
        }
        return team;
    }

    private MatchListingItem toListingItem(MatchListing listing) {
        return new MatchListingItem(
                listing.getId(),
                new SportResponse(listing.getSport().getId(), listing.getSport().getSportName()),
                listing.getFormat(),
                toParticipant(listing.getPostedByUser(), listing.getPostedByTeam()),
                listing.getLocation(),
                listing.getProposedDatetime(),
                listing.getStatus(),
                listing.getMatch() == null ? null : listing.getMatch().getId(),
                listing.getCreatedAt()
        );
    }

    private ListingResponseItem toResponseItem(MatchListingResponse response) {
        return new ListingResponseItem(
                response.getId(),
                response.getListing().getId(),
                toParticipant(response.getResponderUser(), response.getResponderTeam()),
                response.getStatus(),
                response.getCreatedAt()
        );
    }

    private MatchParticipantResponse toParticipant(User user, Team team) {
        if (user != null) {
            return new MatchParticipantResponse(user.getId(), null, user.getName() + " " + user.getSurname(), user.getImageUrl());
        }
        return new MatchParticipantResponse(null, team.getId(), team.getName(), team.getLogoUrl());
    }
}
