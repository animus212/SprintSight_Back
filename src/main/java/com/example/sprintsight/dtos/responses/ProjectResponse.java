package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UserSummaryResponse createdBy
) {}
