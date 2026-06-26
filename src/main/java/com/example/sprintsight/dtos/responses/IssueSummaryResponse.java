package com.example.sprintsight.dtos.responses;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record IssueSummaryResponse(
        UUID id,
        String title,
        String description,
        IssueTypeConfigurationResponse type,
        IssuePriorityConfigurationResponse priority,
        IssueStatusConfigurationResponse status,
        Integer storyPoints,
        UserSummaryResponse assignedTo,
        Instant createdAt,
        Set<ComponentSummaryResponse> components
) {}
