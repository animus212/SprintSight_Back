package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.IssuePriority;
import com.example.sprintsight.entities.IssueStatus;
import com.example.sprintsight.entities.IssueType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        String title,
        String description,
        IssueType type,
        IssuePriority priority,
        IssueStatus status,
        Integer storyPoints,
        String fixVersion,
        ProjectResponse project,
        UserSummaryResponse createdBy,
        UserSummaryResponse assignedTo,
        Set<ComponentResponse> components,
        Instant createdAt,
        Instant updatedAt
) {}
