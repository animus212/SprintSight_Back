package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String username,
        String fullName
) {}
