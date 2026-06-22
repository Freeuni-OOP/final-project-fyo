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
    void creatingTeamAddsCaptainMembershipAndJoiningAddsMember() {
        List<User> users = userRepository.findAll();
        Sport sport = sportRepository.findAll().getFirst();
        User captain = users.get(0);
        User member = users.get(1);

        TeamDetailsResponse created = teamService.createTeam(new CreateTeamRequest(
                "Test Team " + UUID.randomUUID(),
                sport.getId(),
                "Tbilisi",
                "Integration test team",
                "https://example.com/logo.png",
                3,
                true,
                captain.getId()
        ));

        assertThat(created.openSpots()).isEqualTo(2);
        assertThat(created.members()).hasSize(1);
        assertThat(created.members().getFirst().userId()).isEqualTo(captain.getId());
        assertThat(created.members().getFirst().role()).isEqualTo(TeamMemberRole.CAPTAIN);

        TeamDetailsResponse joined = teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId()));

        assertThat(joined.openSpots()).isEqualTo(1);
        assertThat(joined.members()).hasSize(2);
        assertThat(joined.members())
                .extracting(teamMember -> teamMember.userId())
                .containsExactly(captain.getId(), member.getId());
        assertThat(joined.members().get(1).role()).isEqualTo(TeamMemberRole.MEMBER);

        assertThatThrownBy(() -> teamService.joinTeam(created.id(), new JoinTeamRequest(member.getId())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is already a team member");
    }
}
