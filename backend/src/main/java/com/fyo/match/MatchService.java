package com.fyo.match;

import com.fyo.domain.Match;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchRequest;
import com.fyo.domain.MatchRequestStatus;
import com.fyo.domain.MatchStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.User;
import com.fyo.match.dto.CreateMatchRequest;
import com.fyo.match.dto.MatchParticipantResponse;
import com.fyo.match.dto.MatchRequestResponse;
import com.fyo.match.dto.MatchResponse;
import com.fyo.match.dto.SportResponse;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.MatchRequestRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamRepository;
import com.fyo.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public MatchService(
            MatchRepository matchRepository,
            MatchRequestRepository matchRequestRepository,
            SportRepository sportRepository,
            UserRepository userRepository,
            TeamRepository teamRepository
    ) {
        this.matchRepository = matchRepository;
        this.matchRequestRepository = matchRequestRepository;
        this.sportRepository = sportRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(Long userId, Long teamId) {
        List<Match> matches;
        if (userId != null) {
            matches = matchRepository.findByHomeUserIdOrAwayUserId(userId, userId);
        } else if (teamId != null) {
            matches = matchRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);
        } else {
            matches = matchRepository.findAll();
        }
        return matches.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        return toResponse(match);
    }

    @Transactional
    public MatchRequestResponse createMatchRequest(CreateMatchRequest request) {
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found"));

        MatchRequest matchRequest;
        if (isUserRequest(request)) {
            if (!request.actingUserId().equals(request.requesterUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requesting user can create this request");
            }
            if (request.requesterUserId().equals(request.opponentUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user cannot request a match with themselves");
            }
            User requester = requireUser(request.requesterUserId(), "Requester user not found");
            User opponent = requireUser(request.opponentUserId(), "Opponent user not found");
            matchRequest = MatchRequest.oneVsOne(
                    sport,
                    requester,
                    opponent,
                    request.location(),
                    request.proposedDatetime()
            );
        } else if (isTeamRequest(request)) {
            Team requester = requireTeam(request.requesterTeamId(), "Requester team not found");
            Team opponent = requireTeam(request.opponentTeamId(), "Opponent team not found");
            requireCaptain(requester, request.actingUserId(), "Only the requesting team captain can create this request");
            if (requester.getId().equals(opponent.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A team cannot request a match with itself");
            }
            if (!requester.getSport().getId().equals(sport.getId()) || !opponent.getSport().getId().equals(sport.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team match requests must use both teams' sport");
            }
            matchRequest = MatchRequest.teamVsTeam(
                    sport,
                    requester,
                    opponent,
                    request.location(),
                    request.proposedDatetime()
            );
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide either requesterUserId/opponentUserId or requesterTeamId/opponentTeamId"
            );
        }

        return toRequestResponse(matchRequestRepository.save(matchRequest));
    }

    @Transactional(readOnly = true)
    public List<MatchRequestResponse> getMatchRequests(Long userId, Long teamId, MatchRequestStatus status) {
        List<MatchRequest> requests;
        if (userId != null) {
            requests = matchRequestRepository.findByRequesterUserIdOrOpponentUserId(userId, userId);
        } else if (teamId != null) {
            requests = matchRequestRepository.findByRequesterTeamIdOrOpponentTeamId(teamId, teamId);
        } else if (status != null) {
            requests = matchRequestRepository.findByStatus(status);
        } else {
            requests = matchRequestRepository.findAll();
        }

        return requests.stream()
                .filter(request -> status == null || request.getStatus() == status)
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional
    public MatchRequestResponse acceptMatchRequest(Long requestId, Long actingUserId) {
        MatchRequest request = requireMatchRequest(requestId);
        if (request.getStatus() != MatchRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Match request is not pending");
        }
        requireRecipient(request, actingUserId);

        Match match = request.getFormat() == MatchFormat.ONE_VS_ONE
                ? Match.oneVsOne(
                request.getSport(),
                request.getRequesterUser(),
                request.getOpponentUser(),
                request.getLocation(),
                request.getProposedDatetime())
                : Match.teamVsTeam(
                request.getSport(),
                request.getRequesterTeam(),
                request.getOpponentTeam(),
                request.getLocation(),
                request.getProposedDatetime());

        Match savedMatch = matchRepository.save(match);
        request.accept(savedMatch);
        return toRequestResponse(request);
    }

    @Transactional
    public MatchRequestResponse declineMatchRequest(Long requestId, Long actingUserId) {
        MatchRequest request = requireMatchRequest(requestId);
        if (request.getStatus() != MatchRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Match request is not pending");
        }
        requireRecipient(request, actingUserId);
        request.decline();
        return toRequestResponse(request);
    }

    @Transactional
    public MatchResponse cancelMatch(Long id, Long actingUserId) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        assertCanCancel(match, actingUserId);
        if (match.getStatus() != MatchStatus.UPCOMING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only an upcoming match can be cancelled");
        }

        match.cancel();
        return toResponse(match);
    }

    /**
     * ONE_VS_ONE: either participant may cancel.
     * TEAM_VS_TEAM: only a captain of the home or away team may cancel.
     */
    private void assertCanCancel(Match match, Long actingUserId) {
        if (match.getFormat() == MatchFormat.ONE_VS_ONE) {
            if (actingUserId.equals(idOf(match.getHomeUser())) || actingUserId.equals(idOf(match.getAwayUser()))) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this match");
        }

        if (actingUserId.equals(captainIdOf(match.getHomeTeam())) || actingUserId.equals(captainIdOf(match.getAwayTeam()))) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only a team captain can cancel a team match"
        );
    }

    private boolean isUserRequest(CreateMatchRequest request) {
        return request.requesterUserId() != null
                && request.opponentUserId() != null
                && request.requesterTeamId() == null
                && request.opponentTeamId() == null;
    }

    private boolean isTeamRequest(CreateMatchRequest request) {
        return request.requesterTeamId() != null
                && request.opponentTeamId() != null
                && request.requesterUserId() == null
                && request.opponentUserId() == null;
    }

    private User requireUser(Long id, String message) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }

    private Team requireTeam(Long id, String message) {
        return teamRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }

    private MatchRequest requireMatchRequest(Long id) {
        return matchRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match request not found"));
    }

    private void requireRecipient(MatchRequest request, Long actingUserId) {
        if (request.getFormat() == MatchFormat.ONE_VS_ONE) {
            if (actingUserId.equals(request.getOpponentUser().getId())) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requested user can manage this match request");
        }
        requireCaptain(request.getOpponentTeam(), actingUserId, "Only the requested team captain can manage this match request");
    }

    private void requireCaptain(Team team, Long actingUserId, String message) {
        if (!actingUserId.equals(captainIdOf(team))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    private Long captainIdOf(Team team) {
        return team == null || team.getCaptain() == null ? null : team.getCaptain().getId();
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                toSportResponse(match.getSport()),
                match.getFormat(),
                toParticipantResponse(match.getHomeUser(), match.getHomeTeam()),
                toParticipantResponse(match.getAwayUser(), match.getAwayTeam()),
                match.getLocation(),
                match.getProposedDatetime(),
                match.getStatus(),
                match.getCreatedAt()
        );
    }

    private MatchRequestResponse toRequestResponse(MatchRequest request) {
        return new MatchRequestResponse(
                request.getId(),
                toSportResponse(request.getSport()),
                request.getFormat(),
                toParticipantResponse(request.getRequesterUser(), request.getRequesterTeam()),
                toParticipantResponse(request.getOpponentUser(), request.getOpponentTeam()),
                request.getLocation(),
                request.getProposedDatetime(),
                request.getStatus(),
                request.getMatch() == null ? null : request.getMatch().getId(),
                request.getCreatedAt()
        );
    }

    private SportResponse toSportResponse(Sport sport) {
        return new SportResponse(sport.getId(), sport.getSportName());
    }

    private MatchParticipantResponse toParticipantResponse(User user, Team team) {
        if (user != null) {
            return new MatchParticipantResponse(user.getId(), null, user.getName() + " " + user.getSurname(), user.getImageUrl());
        }
        return new MatchParticipantResponse(null, team.getId(), team.getName(), team.getLogoUrl());
    }
}
