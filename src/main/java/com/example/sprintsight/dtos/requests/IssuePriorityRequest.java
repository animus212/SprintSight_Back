package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.*;

public record IssuePriorityRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50)
        String name,

        @NotNull(message = "Order index is required")
        @Min(value = 0, message = "Order index must be non-negative")
        Integer orderIndex,

        boolean isDefault
) {}
