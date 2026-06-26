package com.example.sprintsight.dtos.responses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ComponentResponse(
        UUID id,
        String name,
        String description,
        List<IssueResponse> issues
) {
    public ComponentResponse {
        if (issues == null) {
            issues = new ArrayList<>();
        }
    }
}
