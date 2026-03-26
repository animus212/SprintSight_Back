package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record UpdateUserPutRequest(
        @NotNull(message = "User ID is required")
        UUID id,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        // Optional — only provided when user wants to change password
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password,

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio
) {}
