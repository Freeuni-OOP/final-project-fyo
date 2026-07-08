package com.fyo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fyo.auth.dto.AuthUserResponse;
import com.fyo.auth.dto.SignupRequest;
import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Pure unit tests: the repository and the token verifier are mocked, so these
 * run without a database or Firebase credentials.
 */
class AuthServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final FirebaseTokenVerifier tokenVerifier = mock(FirebaseTokenVerifier.class);
    private final AuthService authService = new AuthService(userRepository, tokenVerifier);

    /** Makes {@code tokenVerifier.verify("id-token")} yield a token with the given claims. */
    private void stubVerifiedToken(String uid, String email) {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(token.getEmail()).thenReturn(email);
        when(tokenVerifier.verify("id-token")).thenReturn(token);
    }

    @Test
    void signupCreatesUserFromTokenAndProfile() {
        stubVerifiedToken("uid-1", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-1")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthUserResponse response = authService.signup("id-token", new SignupRequest(" Ana ", " Kobalia ", null));

        assertThat(response.firebaseUid()).isEqualTo("uid-1");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.surname()).isEqualTo("Kobalia");
        assertThat(response.username()).isEqualTo("ana"); // derived from the email local part
        assertThat(response.onboarding()).isTrue();
    }

    @Test
    void signupIsIdempotentForExistingFirebaseUser() {
        stubVerifiedToken("uid-1", "ana@example.com");
        User existing = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-1")).thenReturn(Optional.of(existing));

        AuthUserResponse response = authService.signup("id-token", SignupRequest.empty());

        assertThat(response.username()).isEqualTo("ana");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupRejectsTokenWithoutEmail() {
        stubVerifiedToken("uid-1", null);

        assertThatThrownBy(() -> authService.signup("id-token", SignupRequest.empty()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("no email claim");
    }

    @Test
    void signupRejectsMalformedEmail() {
        stubVerifiedToken("uid-1", "not-an-email");

        assertThatThrownBy(() -> authService.signup("id-token", SignupRequest.empty()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("malformed email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupRejectsEmailWithoutDomainDot() {
        stubVerifiedToken("uid-1", "ana@localhost");

        assertThatThrownBy(() -> authService.signup("id-token", SignupRequest.empty()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("malformed email");
    }

    @Test
    void signupRejectsDuplicateEmailWithConflict() {
        stubVerifiedToken("uid-3", "taken@example.com");
        when(userRepository.findByFirebaseUid("uid-3")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup("id-token", SignupRequest.empty()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupResolvesUsernameCollisionsWithSuffix() {
        stubVerifiedToken("uid-2", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-2")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("ana")).thenReturn(true);
        when(userRepository.existsByUsername("ana2")).thenReturn(true);
        when(userRepository.existsByUsername("ana3")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthUserResponse response = authService.signup("id-token", SignupRequest.empty());

        assertThat(response.username()).isEqualTo("ana3");
    }

    @Test
    void signupPrefersRequestedUsername() {
        stubVerifiedToken("uid-4", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-4")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("cool.kid")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthUserResponse response = authService.signup("id-token", new SignupRequest(null, null, " cool.kid "));

        assertThat(response.username()).isEqualTo("cool.kid");
    }

    @Test
    void signupPropagatesTokenVerificationFailure() {
        when(tokenVerifier.verify("bad-token"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token"));

        assertThatThrownBy(() -> authService.signup("bad-token", SignupRequest.empty()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(userRepository);
    }

    @Test
    void loginReturnsExistingUser() {
        stubVerifiedToken("uid-1", "ana@example.com");
        User existing = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        when(userRepository.findByFirebaseUid("uid-1")).thenReturn(Optional.of(existing));

        AuthUserResponse response = authService.login("id-token");

        assertThat(response.firebaseUid()).isEqualTo("uid-1");
        assertThat(response.email()).isEqualTo("ana@example.com");
    }

    @Test
    void loginRejectsUnknownUserWithNotFound() {
        stubVerifiedToken("uid-9", "ghost@example.com");
        when(userRepository.findByFirebaseUid("uid-9")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("id-token"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .hasMessageContaining("sign up first");
    }

    @Test
    void loginPropagatesTokenVerificationFailure() {
        when(tokenVerifier.verify("bad-token"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token"));

        assertThatThrownBy(() -> authService.login("bad-token"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verifyNoInteractions(userRepository);
    }
}
