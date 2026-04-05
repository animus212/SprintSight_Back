package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(
                regexp = "^(?=.{3,50}$)(?!.*[._-]{2})[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*$",
                message = "Username must only contain letters, numbers, '.', '_' or '-'"
        )
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Full name must not be blank")
        String fullName
) {}
