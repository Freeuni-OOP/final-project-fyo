package com.fyo.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fyo.domain.Sport;
import com.fyo.domain.TeamMemberRole;
import com.fyo.domain.User;
import com.fyo.repository.SportRepository;
import com.fyo.repository.UserRepository;
import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.JoinTeamRequest;
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

        TeamDetailsResponse joined = teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId()));

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
        teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId()));

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is already a team member");
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
                captain.getId()
        )))
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
                -1L
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Captain user not found");
    }

    @Test
    void joinTeamRejectsNonRecruitingTeam() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 3, false);

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Team is not recruiting");
    }

    @Test
    void joinTeamRejectsFullTeam() {
        List<User> users = userRepository.findAll();
        User captain = users.get(0);
        User member = users.get(1);
        TeamDetailsResponse created = createTeam(captain, 1, true);

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Team has no open spots");
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
                captain.getId()
        ));
    }
}
