package com.fyo.auth;

import com.fyo.auth.dto.AuthUserResponse;
import com.fyo.auth.dto.SignupRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse signup(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody(required = false) SignupRequest request
    ) {
        SignupRequest profile = request != null ? request : SignupRequest.empty();
        return authService.signup(extractBearerToken(authorization), profile);
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return authService.login(extractBearerToken(authorization));
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
