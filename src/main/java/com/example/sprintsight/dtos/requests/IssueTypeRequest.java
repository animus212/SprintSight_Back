package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.dtos.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueTypeRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        String name,

        boolean isDefault
) {}
