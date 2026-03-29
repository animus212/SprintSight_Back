package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.dtos.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "Project name is required", groups = {ValidationGroups.Put.class})
        @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
        String name,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description
) {}
