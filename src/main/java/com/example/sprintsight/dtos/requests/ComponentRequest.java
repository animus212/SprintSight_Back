package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.dtos.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentRequest(
        @NotBlank(message = "Name is required", groups = { ValidationGroups.Post.class, ValidationGroups.Put.class })
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {}
