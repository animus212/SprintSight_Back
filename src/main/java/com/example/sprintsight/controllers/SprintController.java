package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.CloseSprintRequest;
import com.example.sprintsight.dtos.requests.SprintRequest;
import com.example.sprintsight.dtos.requests.StartSprintRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.SprintIssueResponse;
import com.example.sprintsight.dtos.responses.SprintResponse;
import com.example.sprintsight.dtos.responses.SprintSummaryResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects/{projectId}/sprints", produces = MediaType.APPLICATION_JSON_VALUE)
public class SprintController {
    private final SprintService sprintService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SprintSummaryResponse>>> getSprints(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Sprints retrieved successfully",
                sprintService.getSprints(projectId, principal.getId())));
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Sprint retrieved successfully",
                sprintService.getSprint(sprintId, principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SprintSummaryResponse>> createSprint(
            @PathVariable UUID projectId,
            @Valid @RequestBody SprintRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SprintSummaryResponse sprint = sprintService.createSprint(request, projectId, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Sprint created successfully", sprint));
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<ApiResponse<SprintSummaryResponse>> updateSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody SprintRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Sprint updated successfully",
                sprintService.updateSprint(request, sprintId, principal.getId())));
    }

    @PostMapping("/{sprintId}/start")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody StartSprintRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Sprint started successfully",
                sprintService.startSprint(request, sprintId, principal.getId())));
    }

    @PostMapping("/{sprintId}/close")
    public ResponseEntity<ApiResponse<SprintResponse>> closeSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody CloseSprintRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Sprint closed successfully",
                sprintService.closeSprint(request, sprintId, principal.getId())));
    }

    @PostMapping("/{sprintId}/issues/{issueId}")
    public ResponseEntity<ApiResponse<SprintIssueResponse>> addIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SprintIssueResponse entry = sprintService.addIssueToSprint(sprintId, issueId, principal.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Issue added to sprint", entry));
    }

    @DeleteMapping("/{sprintId}/issues/{issueId}")
    public ResponseEntity<ApiResponse<Void>> removeIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        sprintService.removeIssueFromSprint(sprintId, issueId, principal.getId());

        return ResponseEntity.ok(new ApiResponse<>("Issue removed from sprint", null));
    }

    @GetMapping("/{sprintId}/prediction")
    public String getPrediction(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return sprintService.predict(sprintId);
    }
}
