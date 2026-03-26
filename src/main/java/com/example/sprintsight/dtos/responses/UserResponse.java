package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.Role;
import com.example.sprintsight.entities.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String bio,
        Role role
) {}
