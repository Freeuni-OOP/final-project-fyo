package com.fyo.auth;

import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the authenticated local {@link User} from an Authorization Bearer
 * Firebase ID token. Used by profile endpoints that require ownership checks.
 */
@Service
public class CurrentUserService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseTokenVerifier tokenVerifier;
    private final UserRepository userRepository;

    public CurrentUserService(FirebaseTokenVerifier tokenVerifier, UserRepository userRepository) {
        this.tokenVerifier = tokenVerifier;
        this.userRepository = userRepository;
    }

    public User requireCurrentUser(String authorization) {
        String token = extractBearerToken(authorization);
        FirebaseToken firebaseToken = tokenVerifier.verify(token);
        return userRepository.findByFirebaseUid(firebaseToken.getUid())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No local account for this Firebase user"));
    }

    private static String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing Bearer token in Authorization header");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Missing Bearer token in Authorization header");
        }
        return token;
    }
}
