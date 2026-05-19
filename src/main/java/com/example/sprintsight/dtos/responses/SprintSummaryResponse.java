package com.example.sprintsight.dtos.responses;

import com.example.sprintsight.entities.SprintStatus;

import java.time.LocalDate;
import java.util.UUID;

public record SprintSummaryResponse(
        UUID id,
        String name,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate
) {}
