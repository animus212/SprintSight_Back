package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.IssuePriority;
import com.example.sprintsight.entities.IssueStatus;
import com.example.sprintsight.entities.IssueType;

import java.time.Instant;
import java.util.UUID;

public record IssueSummaryResponse(
        UUID id,
        String title,
        IssueType type,
        IssuePriority priority,
        IssueStatus status,
        Integer storyPoints,
        UserSummaryResponse assignedTo,
        Instant createdAt
) {}
