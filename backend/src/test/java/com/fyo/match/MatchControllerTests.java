package com.fyo.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import com.fyo.match.dto.MatchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchControllerTests {

    private final MatchService matchService = mock(MatchService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final MatchController controller = new MatchController(matchService, currentUserService);

    private static final User CURRENT = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");

    @Test
    void cancelMatchUsesAuthenticatedUser() {
        MatchResponse cancelled = mock(MatchResponse.class);
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(matchService.cancelMatch(7L, CURRENT.getId())).thenReturn(cancelled);

        assertThat(controller.cancelMatch("Bearer token", 7L)).isEqualTo(cancelled);
        verify(matchService).cancelMatch(7L, CURRENT.getId());
    }

    @Test
    void getMyMatchesUsesAuthenticatedUser() {
        when(currentUserService.requireCurrentUser("Bearer token")).thenReturn(CURRENT);
        when(matchService.getMatches(CURRENT.getId(), null)).thenReturn(List.of());

        assertThat(controller.getMyMatches("Bearer token")).isEmpty();
        verify(matchService).getMatches(CURRENT.getId(), null);
    }
}
