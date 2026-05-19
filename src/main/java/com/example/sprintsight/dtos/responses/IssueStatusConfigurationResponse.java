package com.example.sprintsight.dtos.responses;


import java.util.UUID;

public record IssueStatusConfigurationResponse(
        UUID id,
        String name,
        int orderIndex,
        boolean isCompleted,
        boolean isDefault
) {}
