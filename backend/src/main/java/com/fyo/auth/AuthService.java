package com.fyo.auth;

import com.fyo.auth.dto.AuthUserResponse;
import com.fyo.auth.dto.SignupRequest;
import com.fyo.domain.User;
import com.fyo.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
public class AuthService {

    /**
     * Practical shape check (something@domain.tld, no whitespace) applied to
     * the email claim of the verified token. Firebase normally guarantees a
     * valid email, so this only guards against malformed claims (e.g. custom
     * tokens) reaching the unique-email column. Deliverability is out of
     * scope. Bean Validation's @Email is not usable here because the email
     * never arrives through a request DTO — only inside the token.
     */
    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final FirebaseTokenVerifier tokenVerifier;

    public AuthService(UserRepository userRepository, FirebaseTokenVerifier tokenVerifier) {
        this.userRepository = userRepository;
        this.tokenVerifier = tokenVerifier;
    }

    /**
     * Idempotent: if the Firebase user already has a local account it is
     * returned as-is, so a retried signup (e.g. after a network failure right
     * after the Firebase account was created) does not fail.
     */
    @Transactional
    public AuthUserResponse signup(String idToken, SignupRequest request) {
        FirebaseToken token = tokenVerifier.verify(idToken);
        String email = token.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firebase token carries no email claim");
        }
        if (!EMAIL_FORMAT.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firebase token carries a malformed email address");
        }

        return userRepository.findByFirebaseUid(token.getUid())
                .map(AuthUserResponse::from)
                .orElseGet(() -> AuthUserResponse.from(createUser(token.getUid(), email, request)));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse login(String idToken) {
        FirebaseToken token = tokenVerifier.verify(idToken);
        User user = userRepository.findByFirebaseUid(token.getUid())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account for this user yet. Please sign up first."));
        return AuthUserResponse.from(user);
    }

    private User createUser(String firebaseUid, String email, SignupRequest request) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        User user = new User(
                firebaseUid,
                trimmedOrEmpty(request.name()),
                trimmedOrEmpty(request.surname()),
                resolveUsername(request.username(), email),
                email
        );
        // Profile is incomplete until the user finishes onboarding in the app.
        // The onboarding flow (POST /api/onboarding, OnboardingService on the
        // feature/onboarding-v2 branch until it merges) is what completes it.
        user.setOnboarding(true);
        return userRepository.save(user);
    }

    private String resolveUsername(String requested, String email) {
        String base;
        if (requested != null && !requested.isBlank()) {
            base = requested.trim();
        } else {
            int at = email.indexOf('@');
            base = (at > 0 ? email.substring(0, at) : email).replaceAll("[^a-zA-Z0-9._-]", "");
        }
        if (base.isBlank()) {
            base = "player";
        }

        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String trimmedOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
