package com.fyo.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.Sport;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.JoinRequestResponse;
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
class JoinRequestServiceTests {
    @Autowired
    private TeamService teamService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Test
    void requestToJoinCreatesPendingRequest() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, true);

        JoinRequestResponse response = teamService.requestToJoin(team.id(), applicant.getId());

        assertThat(response.userId()).isEqualTo(applicant.getId());
        assertThat(response.teamId()).isEqualTo(team.id());
        assertThat(response.status().name()).isEqualTo("PENDING");
    }

    @Test
    void requestToJoinRejectsDuplicateRequest() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, true);
        teamService.requestToJoin(team.id(), applicant.getId());

        assertThatThrownBy(() -> teamService.requestToJoin(team.id(), applicant.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Join request already exists");
    }

    @Test
    void requestToJoinRejectsNonRecruitingTeam() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, false);

        assertThatThrownBy(() -> teamService.requestToJoin(team.id(), applicant.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Team is not recruiting");
    }

    @Test
    void requestToJoinRejectsExistingMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);

        TeamDetailsResponse team = createTeam(captain, 5, true);

        assertThatThrownBy(() -> teamService.requestToJoin(team.id(), captain.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is already a team member");
    }

    @Test
    void acceptJoinRequestAddsMember() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, true);
        JoinRequestResponse request = teamService.requestToJoin(team.id(), applicant.getId());

        JoinRequestResponse accepted = teamService.acceptJoinRequest(team.id(), request.id(), captain.getId());

        assertThat(accepted.status().name()).isEqualTo("ACCEPTED");

        TeamDetailsResponse updated = teamService.getTeam(team.id());
        assertThat(updated.openSpots()).isEqualTo(3);
        assertThat(updated.members()).hasSize(2);
        assertThat(updated.members())
                .anyMatch(m -> m.userId().equals(applicant.getId()));
    }

    @Test
    void declineJoinRequestRejectsApplicant() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, true);
        JoinRequestResponse request = teamService.requestToJoin(team.id(), applicant.getId());

        JoinRequestResponse declined = teamService.declineJoinRequest(team.id(), request.id(), captain.getId());

        assertThat(declined.status().name()).isEqualTo("DECLINED");

        TeamDetailsResponse updated = teamService.getTeam(team.id());
        assertThat(updated.members()).hasSize(1);
    }

    @Test
    void acceptAlreadyAcceptedRequestThrows() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant = users.get(1);

        TeamDetailsResponse team = createTeam(captain, 5, true);
        JoinRequestResponse request = teamService.requestToJoin(team.id(), applicant.getId());
        teamService.acceptJoinRequest(team.id(), request.id(), captain.getId());

        assertThatThrownBy(() -> teamService.acceptJoinRequest(team.id(), request.id(), captain.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Request is not pending");
    }

    @Test
    void getPendingRequestsReturnsOnlyPending() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User applicant1 = users.get(1);
        User applicant2 = users.get(2);

        TeamDetailsResponse team = createTeam(captain, 5, true);
        JoinRequestResponse req1 = teamService.requestToJoin(team.id(), applicant1.getId());
        teamService.requestToJoin(team.id(), applicant2.getId());
        teamService.declineJoinRequest(team.id(), req1.id(), captain.getId());

        List<JoinRequestResponse> pending = teamService.getPendingJoinRequests(team.id());

        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().userId()).isEqualTo(applicant2.getId());
    }

    private TeamDetailsResponse createTeam(User captain, int maxPlayers, boolean isRecruiting) {
        Sport sport = sportRepository.findAll().getFirst();
        return teamService.createTeam(new CreateTeamRequest(
                "Test Team " + UUID.randomUUID(),
                sport.getId(),
                "Tbilisi",
                "Integration test team",
                "https://example.com/logo.png",
                maxPlayers,
                isRecruiting,
                List.of()
        ), captain.getId());
    }
}