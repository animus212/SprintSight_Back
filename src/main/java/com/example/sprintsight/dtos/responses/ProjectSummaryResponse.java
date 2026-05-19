package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record ProjectSummaryResponse(
        UUID id,
        String name
) {}
