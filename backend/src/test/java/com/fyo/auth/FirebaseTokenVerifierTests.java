package com.fyo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FirebaseTokenVerifierTests {

    private final FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);

    /** Overrides the lazy-init seam so no Google credentials are needed. */
    private final FirebaseTokenVerifier verifier = new FirebaseTokenVerifier() {
        @Override
        FirebaseAuth firebaseAuth() {
            return firebaseAuth;
        }
    };

    @Test
    void verifyReturnsDecodedTokenForValidIdToken() throws FirebaseAuthException {
        FirebaseToken token = mock(FirebaseToken.class);
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(token);

        assertThat(verifier.verify("valid-token")).isSameAs(token);
    }

    @Test
    void verifyMapsFirebaseFailureToUnauthorized() throws FirebaseAuthException {
        when(firebaseAuth.verifyIdToken("bad-token")).thenThrow(mock(FirebaseAuthException.class));

        assertThatThrownBy(() -> verifier.verify("bad-token"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .hasMessageContaining("Invalid or expired Firebase ID token");
    }
}
