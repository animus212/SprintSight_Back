package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.entities.ProjectRole;

public record UpdateProjectMemberRequest(
        ProjectRole projectRole
) {
}
