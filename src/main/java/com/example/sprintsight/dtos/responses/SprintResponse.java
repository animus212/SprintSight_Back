package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.SprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SprintResponse(
        UUID id,
        String name,
        String goal,
        ProjectSummaryResponse project,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant completedAt,
        int totalIssues,
        int completedIssues,
        int addedAfterStartCount,
        List<SprintIssueResponse> issues
) {}
