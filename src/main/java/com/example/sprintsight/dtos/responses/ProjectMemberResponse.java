package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.ProjectRole;

import java.time.Instant;

public record ProjectMemberResponse(
        UserSummaryResponse member,
        ProjectRole projectRole,
        Instant joinedAt
) {}
