package com.novamarket.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<ValidationError> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now(), null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(status, error, message, path, Instant.now(), validationErrors);
    }

    public record ValidationError(String field, String message) {
    }
}