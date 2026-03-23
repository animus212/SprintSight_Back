package com.example.sprintsight.dtos.responses;

import java.time.Instant;

public record ApiResponse<T>(String message, T data, Instant timestamp) {
    public ApiResponse(String message, T data) {
        this(message, data, Instant.now());
    }
}
