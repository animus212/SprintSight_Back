package com.example.sprintsight.dtos.responses;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UserSummaryResponse author,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}
