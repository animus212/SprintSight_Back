package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SprintRequest(
        @NotBlank(message = "Sprint name is required")
        @Size(max = 100, message = "Sprint name must not exceed 100 characters")
        String name,

        @Size(max = 2000, message = "Goal must not exceed 2000 characters")
        String goal,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDate endDate
) {}
