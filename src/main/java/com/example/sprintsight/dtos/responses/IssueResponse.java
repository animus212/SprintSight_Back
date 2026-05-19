package com.example.sprintsight.dtos.responses;

import java.time.Instant;
import java.util.Set;
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
        Set<ComponentResponse> components,
        Instant createdAt,
        Instant updatedAt
) {}
