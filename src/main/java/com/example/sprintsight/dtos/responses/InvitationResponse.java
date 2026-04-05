package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.InvitationStatus;
import com.example.sprintsight.entities.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UserSummaryResponse sender,
        UserSummaryResponse receiver,
        ProjectRole intendedRole,
        InvitationStatus status,
        Instant createdAt,
        Instant respondedAt
) {}
