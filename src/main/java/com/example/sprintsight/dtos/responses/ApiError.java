package com.example.sprintsight.dtos.responses;

import java.time.Instant;
import java.util.List;

public record ApiError(String message, List<FieldValidationError> errors, Instant timestamp) {
    public ApiError(String message, List<FieldValidationError> errors) {
        this(message, errors, Instant.now());
    }

    public ApiError(String message) {
        this(message, List.of(), Instant.now());
    }
}