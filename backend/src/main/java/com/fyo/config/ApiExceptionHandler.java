package com.fyo.config;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Spring's default error body has no field for the reason we pass to
 * {@link ResponseStatusException}, so every rejection reached the UI as a bare
 * "Request failed (409)". Re-serialise the reason under `message` — the key the
 * frontend already reads in http.ts and authApi.ts.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String reason = exception.getReason();
        return ResponseEntity
                .status(status)
                .body(new ApiErrorResponse(status.value(), reason != null ? reason : status.toString()));
    }
}
