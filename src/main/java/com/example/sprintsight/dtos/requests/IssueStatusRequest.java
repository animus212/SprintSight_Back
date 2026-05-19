package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueStatusRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50)
        String name,

        @NotNull(message = "Order index is required")
        @Min(value = 0)
        Integer orderIndex,

        boolean isCompleted,
        boolean isDefault
) {}
