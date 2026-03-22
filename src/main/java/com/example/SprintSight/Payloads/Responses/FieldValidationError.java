package com.example.SprintSight.Payloads.Responses;

public record FieldValidationError(
        String field,
        String error
) {}
