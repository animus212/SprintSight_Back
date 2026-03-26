package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.Size;

public record UpdateProjectPatchRequest(
        @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
        String name,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description
) {}
