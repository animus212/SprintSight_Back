package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.dtos.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidDateRange
public record StartSprintRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {}
