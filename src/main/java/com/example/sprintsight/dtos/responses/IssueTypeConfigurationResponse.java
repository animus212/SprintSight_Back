package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record IssueTypeConfigurationResponse(
        UUID id,
        String name,
        boolean isDefault
) {}
