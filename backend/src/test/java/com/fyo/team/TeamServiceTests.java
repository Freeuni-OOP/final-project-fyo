package com.fyo.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.JoinRequestStatus;
import com.fyo.domain.Sport;
import com.fyo.domain.TeamMemberRole;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.JoinRequestResponse;
import com.fyo.team.dto.MyJoinRequestResponse;
import com.fyo.team.dto.TeamDetailsResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class TeamServiceTests {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Test
    void createTeamAddsCaptainMembership() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);

        TeamDetailsResponse created = createTeam(captain, 3, true);

        assertThat(created.openSpots()).isEqualTo(2);
        assertThat(created.members()).hasSize(1);
        assertThat(created.members().getFirst().userId()).isEqualTo(captain.getId());
        assertThat(created.members().getFirst().role()).isEqualTo(TeamMemberRole.CAPTAIN);
    }

    @Test
    void joinTeamAddsMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);

        TeamDetailsResponse created = createTeam(captain, 3, true);

        TeamDetailsResponse joined = teamService.joinTeam(created.id(), member.getId());

        assertThat(joined.openSpots()).isEqualTo(1);
        assertThat(joined.members()).hasSize(2);
        assertThat(joined.members())
                .extracting(teamMember -> teamMember.userId())
                .contains(captain.getId(), member.getId());
        assertThat(joined.members())
                .anyMatch(teamMember ->
                        teamMember.userId().equals(member.getId())
                                && teamMember.role() == TeamMemberRole.MEMBER
                );
    }

    @Test
    void joinTeamRejectsDuplicateMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);

        TeamDetailsResponse created = createTeam(captain, 3, true);
        teamService.joinTeam(created.id(), member.getId());

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), member.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is already a team member");
    }

    @Test
    void createTeamSeedsTheRosterWithTheChosenPlayers() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User first = users.get(1);
        User second = users.get(2);

        TeamDetailsResponse created =
                createTeam(captain, 5, true, List.of(first.getId(), second.getId()));

        assertThat(created.openSpots()).isEqualTo(2);
        assertThat(created.members()).hasSize(3);
        assertThat(created.members())
                .filteredOn(member -> member.role() == TeamMemberRole.MEMBER)
                .extracting(member -> member.userId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void createTeamIgnoresTheCaptainAndDuplicatesInTheMemberList() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);

        TeamDetailsResponse created = createTeam(
                captain, 3, true, List.of(captain.getId(), member.getId(), member.getId()));

        assertThat(created.members()).hasSize(2);
        assertThat(created.openSpots()).isEqualTo(1);
    }

    @Test
    void createTeamRejectsMoreMembersThanTheRosterSeats() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        List<Long> tooMany = List.of(users.get(1).getId(), users.get(2).getId());

        assertThatThrownBy(() -> createTeam(captain, 2, true, tooMany))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("seats only 1 players besides the captain");
    }

    @Test
    void createTeamRejectsAnUnknownMember() {
        User captain = userRepository.findAll().getFirst();

        assertThatThrownBy(() -> createTeam(captain, 5, true, List.of(-1L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("One or more selected players no longer exist");
    }

    @Test
    void createTeamRejectsInvalidSport() {
        User captain = userRepository.findAll().getFirst();

        assertThatThrownBy(() -> teamService.createTeam(new CreateTeamRequest(
                "Test Team " + UUID.randomUUID(),
                -1L,
                "Tbilisi",
                "Integration test team",
                "https://example.com/logo.png",
                3,
                true,
                List.of()
        ), captain.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Sport not found");
    }

    @Test
    void createTeamRejectsInvalidCaptainUser() {
        Sport sport = sportRepository.findAll().getFirst();

        assertThatThrownBy(() -> teamService.createTeam(new CreateTeamRequest(
                "Test Team " + UUID.randomUUID(),
                sport.getId(),
                "Tbilisi",
                "Integration test team",
                "https://example.com/logo.png",
                3,
                true,
                List.of()
        ), -1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Captain user not found");
    }

    @Test
    void joinTeamRejectsNonRecruitingTeam() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, false);

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), member.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Team is not recruiting");
    }

    @Test
    void joinTeamRejectsFullTeam() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 1, true);

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), member.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Team has no open spots");
    }

    @Test
    void requestToJoinRejectsASecondPendingRequest() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, true);

        teamService.requestToJoin(created.id(), applicant.getId());

        assertThatThrownBy(() -> teamService.requestToJoin(created.id(), applicant.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You have already asked to join this team");
    }

    @Test
    void requestToJoinTellsADeclinedApplicantWhy() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, true);

        JoinRequestResponse request = teamService.requestToJoin(created.id(), applicant.getId());
        teamService.declineJoinRequest(created.id(), request.id(), captain.getId());

        assertThatThrownBy(() -> teamService.requestToJoin(created.id(), applicant.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("This team declined your request to join.");
    }

    @Test
    void requestToJoinRejectsAnExistingMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, true);
        teamService.joinTeam(created.id(), member.getId());

        assertThatThrownBy(() -> teamService.requestToJoin(created.id(), member.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are already on this team.");
    }

    /** Membership outranks roster state, so a captain of a full team hears the useful reason. */
    @Test
    void requestToJoinTellsAMemberOfAFullTeamTheyAreAlreadyOnIt() {
        User captain = userRepository.findAll().getFirst();
        TeamDetailsResponse created = createTeam(captain, 1, true);

        assertThatThrownBy(() -> teamService.requestToJoin(created.id(), captain.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are already on this team.");
    }

    @Test
    void getTeamsForUserListsTheTeamsTheUserPlaysFor() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, true);
        teamService.joinTeam(created.id(), member.getId());

        assertThat(teamService.getTeamsForUser(captain.getId()))
                .filteredOn(myTeam -> myTeam.team().id().equals(created.id()))
                .singleElement()
                .satisfies(myTeam -> assertThat(myTeam.role()).isEqualTo(TeamMemberRole.CAPTAIN));

        assertThat(teamService.getTeamsForUser(member.getId()))
                .filteredOn(myTeam -> myTeam.team().id().equals(created.id()))
                .singleElement()
                .satisfies(myTeam -> assertThat(myTeam.role()).isEqualTo(TeamMemberRole.MEMBER));
    }

    @Test
    void getTeamsForUserRejectsAnUnknownUser() {
        assertThatThrownBy(() -> teamService.getTeamsForUser(-1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getJoinRequestsForUserReportsPendingAndDeclinedRequests() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);
        TeamDetailsResponse pendingTeam = createTeam(captain, 3, true);
        TeamDetailsResponse declinedTeam = createTeam(captain, 3, true);

        teamService.requestToJoin(pendingTeam.id(), applicant.getId());
        JoinRequestResponse declined = teamService.requestToJoin(declinedTeam.id(), applicant.getId());
        teamService.declineJoinRequest(declinedTeam.id(), declined.id(), captain.getId());

        assertThat(teamService.getJoinRequestsForUser(applicant.getId()))
                .filteredOn(request -> List.of(pendingTeam.id(), declinedTeam.id())
                        .contains(request.team().id()))
                .extracting(MyJoinRequestResponse::status)
                .containsExactlyInAnyOrder(JoinRequestStatus.PENDING, JoinRequestStatus.DECLINED);
    }

    /** An accepted request would otherwise duplicate the team in the "my teams" list. */
    @Test
    void getJoinRequestsForUserOmitsAcceptedRequests() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, true);

        JoinRequestResponse request = teamService.requestToJoin(created.id(), applicant.getId());
        teamService.acceptJoinRequest(created.id(), request.id(), captain.getId());

        assertThat(teamService.getJoinRequestsForUser(applicant.getId()))
                .extracting(myRequest -> myRequest.team().id())
                .doesNotContain(created.id());
        assertThat(teamService.getTeamsForUser(applicant.getId()))
                .extracting(myTeam -> myTeam.team().id())
                .contains(created.id());
    }

    private TeamDetailsResponse createTeam(User captain, int maxPlayers, boolean isRecruiting) {
        return createTeam(captain, maxPlayers, isRecruiting, List.of());
    }

    private TeamDetailsResponse createTeam(
            User captain, int maxPlayers, boolean isRecruiting, List<Long> memberUserIds) {
        Sport sport = sportRepository.findAll().getFirst();

        return teamService.createTeam(new CreateTeamRequest(
                "Test Team " + UUID.randomUUID(),
                sport.getId(),
                "Tbilisi",
                "Integration test team",
                "https://example.com/logo.png",
                maxPlayers,
                isRecruiting,
                memberUserIds
        ), captain.getId());
    }
}
