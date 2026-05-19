package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StartSprintRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {}
