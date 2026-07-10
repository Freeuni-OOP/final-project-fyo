package com.fyo.config;

/** The JSON body every failed API call returns. `message` is safe to show a user. */
public record ApiErrorResponse(int status, String message) {
}
