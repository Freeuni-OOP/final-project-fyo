package com.fyo.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.ConversationParticipant;
import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchListingResponseStatus;
import com.fyo.domain.MatchListingStatus;
import com.fyo.domain.MatchStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.Team;
import com.fyo.domain.TeamMember;
import com.fyo.domain.TeamMemberRole;
import com.fyo.domain.User;
import com.fyo.match.dto.AcceptListingResult;
import com.fyo.match.dto.CreateMatchListingRequest;
import com.fyo.match.dto.ListingResponseItem;
import com.fyo.match.dto.MatchListingItem;
import com.fyo.match.dto.RespondToListingRequest;
import com.fyo.repository.ConversationParticipantRepository;
import com.fyo.repository.MatchListingRepository;
import com.fyo.repository.MatchListingResponseRepository;
import com.fyo.repository.MatchRepository;
import com.fyo.repository.SportRepository;
import com.fyo.repository.TeamMemberRepository;
import com.fyo.repository.TeamRepository;
import com.fyo.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class MatchListingServiceTests {

    @Autowired
    private MatchListingService listingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchListingRepository listingRepository;

    @Autowired
    private MatchListingResponseRepository responseRepository;

    @Autowired
    private ConversationParticipantRepository conversationParticipantRepository;

    private Sport anySport() {
        return sportRepository.findAll().getFirst();
    }

    private MatchListingItem postOneVsOne(User poster) {
        return listingService.createListing(poster, new CreateMatchListingRequest(
                anySport().getId(), null, "Tbilisi", OffsetDateTime.now().plusDays(2)));
    }

    @Test
    void acceptingResponseCreatesMatchConversationAndDeclinesOthers() {
        List<User> users = userRepository.findAll();
        User poster = users.get(0);
        User accepted = users.get(1);
        User declined = users.get(2);

        MatchListingItem listing = postOneVsOne(poster);
        ListingResponseItem acceptedResponse =
                listingService.respondToListing(accepted, listing.id(), new RespondToListingRequest(null));
        ListingResponseItem declinedResponse =
                listingService.respondToListing(declined, listing.id(), new RespondToListingRequest(null));

        AcceptListingResult result =
                listingService.acceptResponse(poster, listing.id(), acceptedResponse.id());

        assertThat(result.matchId()).isNotNull();
        assertThat(result.conversationId()).isNotNull();

        var match = matchRepository.findById(result.matchId()).orElseThrow();
        assertThat(match.getFormat()).isEqualTo(MatchFormat.ONE_VS_ONE);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.UPCOMING);
        assertThat(match.getHomeUser().getId()).isEqualTo(poster.getId());
        assertThat(match.getAwayUser().getId()).isEqualTo(accepted.getId());

        var filled = listingRepository.findById(listing.id()).orElseThrow();
        assertThat(filled.getStatus()).isEqualTo(MatchListingStatus.FILLED);
        assertThat(filled.getMatch().getId()).isEqualTo(result.matchId());

        assertThat(responseRepository.findById(declinedResponse.id()).orElseThrow().getStatus())
                .isEqualTo(MatchListingResponseStatus.DECLINED);

        assertThat(conversationParticipantRepository.findByConversationId(result.conversationId()))
                .extracting(participant -> participant.getUser().getId())
                .containsExactlyInAnyOrder(poster.getId(), accepted.getId());
    }

    @Test
    void acceptingSecondTimeReusesSameConversation() {
        List<User> users = userRepository.findAll();
        User poster = users.get(0);
        User responder = users.get(1);

        MatchListingItem listing = postOneVsOne(poster);
        ListingResponseItem response =
                listingService.respondToListing(responder, listing.id(), new RespondToListingRequest(null));
        AcceptListingResult first = listingService.acceptResponse(poster, listing.id(), response.id());

        assertThatThrownBy(() -> listingService.acceptResponse(poster, listing.id(), response.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no longer open");

        assertThat(first.conversationId()).isNotNull();
    }

    @Test
    void onlyPosterCanAccept() {
        List<User> users = userRepository.findAll();
        User poster = users.get(0);
        User responder = users.get(1);
        User outsider = users.get(2);

        MatchListingItem listing = postOneVsOne(poster);
        ListingResponseItem response =
                listingService.respondToListing(responder, listing.id(), new RespondToListingRequest(null));

        assertThatThrownBy(() -> listingService.acceptResponse(outsider, listing.id(), response.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the listing poster");
    }

    @Test
    void posterCannotRespondToOwnListing() {
        User poster = userRepository.findAll().get(0);
        MatchListingItem listing = postOneVsOne(poster);

        assertThatThrownBy(() ->
                listingService.respondToListing(poster, listing.id(), new RespondToListingRequest(null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("your own listing");
    }

    @Test
    void teamAcceptCreatesGroupConversationWithBothRosters() {
        List<User> users = userRepository.findAll();
        User homeCaptain = users.get(0);
        User homePlayer = users.get(1);
        User awayCaptain = users.get(2);
        User awayPlayer = users.get(3);
        Sport sport = anySport();

        Team homeTeam = teamRepository.save(new Team(
                "Home FC", sport, "Tbilisi", null, null, homeCaptain, (short) 5, (short) 3, true));
        Team awayTeam = teamRepository.save(new Team(
                "Away FC", sport, "Tbilisi", null, null, awayCaptain, (short) 5, (short) 3, true));
        teamMemberRepository.save(new TeamMember(homeTeam, homeCaptain, TeamMemberRole.CAPTAIN));
        teamMemberRepository.save(new TeamMember(homeTeam, homePlayer, TeamMemberRole.MEMBER));
        teamMemberRepository.save(new TeamMember(awayTeam, awayCaptain, TeamMemberRole.CAPTAIN));
        teamMemberRepository.save(new TeamMember(awayTeam, awayPlayer, TeamMemberRole.MEMBER));

        MatchListingItem listing = listingService.createListing(homeCaptain, new CreateMatchListingRequest(
                sport.getId(), homeTeam.getId(), "Tbilisi", OffsetDateTime.now().plusDays(3)));
        ListingResponseItem response = listingService.respondToListing(
                awayCaptain, listing.id(), new RespondToListingRequest(awayTeam.getId()));

        AcceptListingResult result = listingService.acceptResponse(homeCaptain, listing.id(), response.id());

        var match = matchRepository.findById(result.matchId()).orElseThrow();
        assertThat(match.getFormat()).isEqualTo(MatchFormat.TEAM_VS_TEAM);
        assertThat(match.getHomeTeam().getId()).isEqualTo(homeTeam.getId());
        assertThat(match.getAwayTeam().getId()).isEqualTo(awayTeam.getId());

        assertThat(conversationParticipantRepository.findByConversationId(result.conversationId()))
                .extracting((ConversationParticipant participant) -> participant.getUser().getId())
                .containsExactlyInAnyOrder(
                        homeCaptain.getId(), homePlayer.getId(), awayCaptain.getId(), awayPlayer.getId());
    }

    @Test
    void nonCaptainCannotPostTeamListing() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        Sport sport = anySport();

        Team team = teamRepository.save(new Team(
                "Strict FC", sport, "Tbilisi", null, null, captain, (short) 5, (short) 4, true));
        teamMemberRepository.save(new TeamMember(team, captain, TeamMemberRole.CAPTAIN));
        teamMemberRepository.save(new TeamMember(team, member, TeamMemberRole.MEMBER));

        assertThatThrownBy(() -> listingService.createListing(member, new CreateMatchListingRequest(
                sport.getId(), team.getId(), "Tbilisi", OffsetDateTime.now().plusDays(1))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the team captain");
    }
}
