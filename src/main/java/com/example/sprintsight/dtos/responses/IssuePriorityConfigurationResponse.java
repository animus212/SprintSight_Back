package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record IssuePriorityConfigurationResponse(
        UUID id,
        String name,
        int orderIndex,
        boolean isDefault
) {}
