package com.example.sprintsight.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String message,
        Integer status,
        String path,
        String method,
        Instant timestamp,
        List<FieldValidationError> fieldErrors,
        String requestId
) {
    public ApiError(String message) {
        this(message, null, null, null, Instant.now(), null, null);
    }

    public ApiError(String message, List<FieldValidationError> fieldErrors) {
        this(message, null, null, null, Instant.now(), fieldErrors, null);
    }
}
