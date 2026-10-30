package com.ashutosh.jobtracker.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** One error shape for the whole API, so the frontend never has to guess. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(LocalDateTime.now(), status, error, message, null);
    }

    public static ApiError of(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ApiError(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}
