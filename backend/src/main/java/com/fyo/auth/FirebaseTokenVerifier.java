package com.fyo.auth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Verifies Firebase ID tokens with the Firebase Admin SDK. The verified token
 * is the only trusted source of user identity (uid, email) — request bodies
 * never are.
 */
@Component
public class FirebaseTokenVerifier {

    public FirebaseToken verify(String idToken) {
        try {
            return firebaseAuth().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token");
        }
    }

    /**
     * The FirebaseApp is initialized on first use, not at startup, so the
     * application (and its tests) can run without Firebase credentials as long
     * as no auth endpoint is called.
     */
    private synchronized FirebaseAuth firebaseAuth() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build());
            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Firebase Admin credentials are not configured. "
                                + "Set GOOGLE_APPLICATION_CREDENTIALS to the service account JSON path."
                );
            }
        }
        return FirebaseAuth.getInstance();
    }
}
