package com.example.SprintSight.Payloads.Requests;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password
) {}
