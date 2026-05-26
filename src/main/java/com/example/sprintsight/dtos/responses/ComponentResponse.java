package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record ComponentResponse(
        UUID id,
        String name,
        String description
) {}
