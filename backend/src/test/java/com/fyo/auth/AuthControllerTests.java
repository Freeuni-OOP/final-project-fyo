package com.fyo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.auth.dto.AuthUserResponse;
import com.fyo.auth.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for the controller's own responsibilities: Bearer-token
 * extraction and defaulting the optional signup body. The service is mocked.
 */
class AuthControllerTests {

    private static final AuthUserResponse SOME_USER =
            new AuthUserResponse(1L, "uid-1", "ana", "ana@example.com", "Ana", "Kobalia", null, true);

    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void signupExtractsBearerTokenAndPassesProfile() {
        SignupRequest profile = new SignupRequest("Ana", "Kobalia", null);
        when(authService.signup("token-123", profile)).thenReturn(SOME_USER);

        AuthUserResponse response = controller.signup("Bearer token-123", profile);

        assertThat(response).isEqualTo(SOME_USER);
        verify(authService).signup("token-123", profile);
    }

    @Test
    void signupDefaultsToEmptyProfileWhenBodyMissing() {
        when(authService.signup("token-123", SignupRequest.empty())).thenReturn(SOME_USER);

        AuthUserResponse response = controller.signup("Bearer token-123", null);

        assertThat(response).isEqualTo(SOME_USER);
        verify(authService).signup("token-123", SignupRequest.empty());
    }

    @Test
    void signupRejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> controller.signup(null, null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .hasMessageContaining("Missing Bearer token");
        verifyNoInteractions(authService);
    }

    @Test
    void signupRejectsNonBearerAuthorizationHeader() {
        assertThatThrownBy(() -> controller.signup("Basic dXNlcjpwdw==", null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(authService);
    }

    @Test
    void signupRejectsBlankBearerToken() {
        assertThatThrownBy(() -> controller.signup("Bearer   ", null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(authService);
    }

    @Test
    void loginExtractsBearerToken() {
        when(authService.login("token-123")).thenReturn(SOME_USER);

        assertThat(controller.login("Bearer token-123")).isEqualTo(SOME_USER);
        verify(authService).login("token-123");
    }

    @Test
    void loginRejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> controller.login(null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(authService);
    }
}
