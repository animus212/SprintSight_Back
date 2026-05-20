package com.example.sprintsight.dtos.responses;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        String title,
        String description,
        IssueTypeConfigurationResponse type,
        IssuePriorityConfigurationResponse priority,
        IssueStatusConfigurationResponse status,
        Integer storyPoints,
        String fixVersion,
        ProjectSummaryResponse project,
        UserSummaryResponse createdBy,
        UserSummaryResponse assignedTo,
        List<ComponentResponse> components,
        Instant createdAt,
        Instant updatedAt
) {}
