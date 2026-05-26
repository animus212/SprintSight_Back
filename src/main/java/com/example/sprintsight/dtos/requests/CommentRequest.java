package com.example.sprintsight.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Content is required")
        @Size(max = 10000, message = "Comment must not exceed 10000 characters")
        String content
) {}
