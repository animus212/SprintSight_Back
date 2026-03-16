package com.example.SprintSight.DTOs;

import java.time.LocalDateTime;

public record ApiResponse<T>(String message, T data, LocalDateTime timestamp) {
    public ApiResponse(String message, T data) {
        this(message, data, LocalDateTime.now());
    }
}
