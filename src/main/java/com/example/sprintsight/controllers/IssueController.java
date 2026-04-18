package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.CreateIssueRequest;
import com.example.sprintsight.dtos.requests.UpdateIssueRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.dtos.responses.IssueSummaryResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.IssueService;
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
@RequestMapping(value = "/api/projects/{projectId}/issues",
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueSummaryResponse>>> getIssues(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Issues retrieved successfully",
                issueService.getProjectIssues(projectId, principal.getId())));
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Issue retrieved successfully",
                issueService.getIssue(issueId, principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        IssueResponse issue = issueService.createIssue(request, projectId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Issue created successfully", issue));
    }

    @PatchMapping("/{issueId}")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @Valid @RequestBody UpdateIssueRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Issue updated successfully",
                issueService.updateIssue(request, issueId, principal.getId())));
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<ApiResponse<Void>> deleteIssue(
            @PathVariable UUID projectId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserPrincipal principal) {
        issueService.deleteIssue(issueId, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Issue deleted successfully", null));
    }
}
