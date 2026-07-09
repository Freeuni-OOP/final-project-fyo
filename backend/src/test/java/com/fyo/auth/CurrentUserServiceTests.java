package com.fyo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CurrentUserServiceTests {

    private final FirebaseTokenVerifier tokenVerifier = mock(FirebaseTokenVerifier.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService =
            new CurrentUserService(tokenVerifier, userRepository);

    @Test
    void requireCurrentUserResolvesLocalUserFromBearerToken() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("uid-1");
        when(tokenVerifier.verify("id-token")).thenReturn(token);

        User user = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-1")).thenReturn(Optional.of(user));

        User resolved = currentUserService.requireCurrentUser("Bearer id-token");

        assertThat(resolved).isSameAs(user);
    }

    @Test
    void requireCurrentUserRejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> currentUserService.requireCurrentUser(null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .hasMessageContaining("Missing Bearer token");
        verifyNoInteractions(tokenVerifier, userRepository);
    }

    @Test
    void requireCurrentUserRejectsNonBearerAuthorizationHeader() {
        assertThatThrownBy(() -> currentUserService.requireCurrentUser("Basic abc"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(tokenVerifier, userRepository);
    }

    @Test
    void requireCurrentUserRejectsBlankBearerToken() {
        assertThatThrownBy(() -> currentUserService.requireCurrentUser("Bearer   "))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(tokenVerifier, userRepository);
    }

    @Test
    void requireCurrentUserReturnsNotFoundWhenLocalUserMissing() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("uid-missing");
        when(tokenVerifier.verify("id-token")).thenReturn(token);
        when(userRepository.findByFirebaseUid("uid-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.requireCurrentUser("Bearer id-token"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .hasMessageContaining("No local account");
    }
}
