package com.fyo.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.team.dto.CreateTeamRequest;
import com.fyo.team.dto.JoinRequestResponse;
import com.fyo.team.dto.TeamDetailsResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class TeamControllerTests {

    private final TeamService teamService = mock(TeamService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final TeamController controller = new TeamController(teamService, currentUserService);

    private static final User CURRENT = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");

    @Test
    void getMyTeamsUsesAuthenticatedUser() {
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(teamService.getTeamsForUser(CURRENT.getId())).thenReturn(List.of());

        assertThat(controller.getMyTeams("Bearer token")).isEmpty();
        verify(teamService).getTeamsForUser(CURRENT.getId());
    }

    @Test
    void createTeamUsesAuthenticatedUserAsCaptain() {
        CreateTeamRequest body = new CreateTeamRequest(
                "FC", 1L, "Tbilisi", null, null, 8, true, List.of());
        TeamDetailsResponse created = mock(TeamDetailsResponse.class);

        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(teamService.createTeam(body, CURRENT.getId())).thenReturn(created);

        assertThat(controller.createTeam("Bearer token", body)).isEqualTo(created);
        verify(teamService).createTeam(body, CURRENT.getId());
    }

    @Test
    void acceptJoinRequestUsesAuthenticatedCaptain() {
        JoinRequestResponse response = mock(JoinRequestResponse.class);
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(teamService.acceptJoinRequest(3L, 9L, CURRENT.getId())).thenReturn(response);

        assertThat(controller.acceptJoinRequest("Bearer token", 3L, 9L)).isEqualTo(response);
    }

    @Test
    void missingTokenDoesNotHitService() {
        when(currentUserService.requireCurrentUser(null))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token"));

        assertThatThrownBy(() -> controller.getMyJoinRequests(null))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(teamService);
    }
}
