package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.IssuePriorityRequest;
import com.example.sprintsight.dtos.requests.IssueStatusRequest;
import com.example.sprintsight.dtos.requests.IssueTypeRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.IssuePriorityConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueStatusConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueTypeConfigurationResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.IssueConfigurationService;
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
@RequestMapping(value = "/api/projects/{projectId}/Configuration", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class IssueConfigurationController {
    private final IssueConfigurationService issueConfigurationService;

    // ── Types ──────────────────────────────────────────────────────────────

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<IssueTypeConfigurationResponse>>> getTypes(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Types retrieved successfully",
                issueConfigurationService.getTypes(projectId, principal.getId())));
    }

    @PostMapping("/types")
    public ResponseEntity<ApiResponse<IssueTypeConfigurationResponse>> createType(
            @PathVariable UUID projectId,
            @Valid @RequestBody IssueTypeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Type created successfully",
                        issueConfigurationService.createType(request, projectId, principal.getId())));
    }

    @PatchMapping("/types/{typeId}")
    public ResponseEntity<ApiResponse<IssueTypeConfigurationResponse>> updateType(
            @PathVariable UUID projectId,
            @PathVariable UUID typeId,
            @Valid @RequestBody IssueTypeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Type updated successfully",
                issueConfigurationService.updateType(request, typeId, projectId, principal.getId())));
    }

    @DeleteMapping("/types/{typeId}")
    public ResponseEntity<ApiResponse<Void>> deleteType(
            @PathVariable UUID projectId,
            @PathVariable UUID typeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        issueConfigurationService.deleteType(typeId, projectId, principal.getId());

        return ResponseEntity.ok(new ApiResponse<>("Type deleted successfully", null));
    }

    // ── Priorities ─────────────────────────────────────────────────────────

    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<IssuePriorityConfigurationResponse>>> getPriorities(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Priorities retrieved successfully",
                issueConfigurationService.getPriorities(projectId, principal.getId())));
    }

    @PostMapping("/priorities")
    public ResponseEntity<ApiResponse<IssuePriorityConfigurationResponse>> createPriority(
            @PathVariable UUID projectId,
            @Valid @RequestBody IssuePriorityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Priority created successfully",
                        issueConfigurationService.createPriority(request, projectId, principal.getId())));
    }

    @PatchMapping("/priorities/{priorityId}")
    public ResponseEntity<ApiResponse<IssuePriorityConfigurationResponse>> updatePriority(
            @PathVariable UUID projectId,
            @PathVariable UUID priorityId,
            @Valid @RequestBody IssuePriorityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Priority updated successfully",
                issueConfigurationService.updatePriority(request, priorityId, projectId, principal.getId())));
    }

    @DeleteMapping("/priorities/{priorityId}")
    public ResponseEntity<ApiResponse<Void>> deletePriority(
            @PathVariable UUID projectId,
            @PathVariable UUID priorityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        issueConfigurationService.deletePriority(priorityId, projectId, principal.getId());

        return ResponseEntity.ok(new ApiResponse<>("Priority deleted successfully", null));
    }

    // ── Statuses ───────────────────────────────────────────────────────────

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<IssueStatusConfigurationResponse>>> getStatuses(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Statuses retrieved successfully",
                issueConfigurationService.getStatuses(projectId, principal.getId())));
    }

    @PostMapping("/statuses")
    public ResponseEntity<ApiResponse<IssueStatusConfigurationResponse>> createStatus(
            @PathVariable UUID projectId,
            @Valid @RequestBody IssueStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Status created successfully",
                        issueConfigurationService.createStatus(request, projectId, principal.getId())));
    }

    @PatchMapping("/statuses/{statusId}")
    public ResponseEntity<ApiResponse<IssueStatusConfigurationResponse>> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID statusId,
            @Valid @RequestBody IssueStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("Status updated successfully",
                issueConfigurationService.updateStatus(request, statusId, projectId, principal.getId())));
    }

    @DeleteMapping("/statuses/{statusId}")
    public ResponseEntity<ApiResponse<Void>> deleteStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID statusId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        issueConfigurationService.deleteStatus(statusId, projectId, principal.getId());

        return ResponseEntity.ok(new ApiResponse<>("Status deleted successfully", null));
    }
}
