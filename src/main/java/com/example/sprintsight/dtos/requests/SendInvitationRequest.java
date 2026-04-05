package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.entities.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendInvitationRequest(
        @NotNull(message = "User ID is required")
        UUID receiverId,

        @NotNull(message = "Project role is required")
        ProjectRole intendedRole
) {}
