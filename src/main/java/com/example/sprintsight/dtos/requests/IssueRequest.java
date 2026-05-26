package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.dtos.validation.ValidationGroups;
import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

public record IssueRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 10000, message = "Description must not exceed 10000 characters")
        String description,

        @NotNull(message = "Issue type is required")
        UUID typeId,

        @NotNull(message = "Priority is required")
        UUID priorityId,

        @NotNull(message = "Status is required", groups = ValidationGroups.Put.class)
        UUID statusId,

        @Min(value = 0, message = "Story points must be non-negative")
        @Max(value = 100, message = "Story points must not exceed 100")
        Integer storyPoints,

        @Size(max = 50, message = "Fix version must not exceed 50 characters")
        String fixVersion,

        UUID assignedTo,

        @Size(max = 50, message = "An issue cannot have more than 50 components")
        Set<UUID> componentIds
) {}
