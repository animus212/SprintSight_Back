package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.entities.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRequest(
        @NotNull(message = "Project role is required")
        ProjectRole projectRole
) {}
