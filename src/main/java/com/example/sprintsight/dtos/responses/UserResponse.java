package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String bio,
        UserRole userRole,
        Instant createdAt,
        Instant updatedAt
) {}
