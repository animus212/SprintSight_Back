package com.example.sprintsight.dtos.requests;

import java.util.UUID;

public record CloseSprintRequest(
        UUID moveUnfinishedToSprintId   // null means move to backlog
) {}
