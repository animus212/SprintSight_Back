package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.IssueStatus;

import java.time.Instant;

public record SprintIssueResponse(
        IssueSummaryResponse issue,
        Instant addedAt,
        boolean addedAfterStart,
        IssueStatus statusAtClosure,    // null if sprint not yet completed
        boolean completedInSprint,
        Instant removedAt               // null if still active in sprint
) {}
