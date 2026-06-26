package com.example.sprintsight.dtos.responses;

import java.util.UUID;

public record ComponentSummaryResponse(UUID id,
                                       String name,
                                       String description) {
}
