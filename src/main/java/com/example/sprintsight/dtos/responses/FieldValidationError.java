package com.example.sprintsight.dtos.responses;

public record FieldValidationError(
        String field,
        String error
) {}
