package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.entities.ProjectRole;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddProjectMemberRequest(
        @NotBlank
        UUID userId,
        @NotBlank
        ProjectRole projectRole
) {}
