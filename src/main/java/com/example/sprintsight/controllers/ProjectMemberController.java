package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Members retrieved successfully",
                projectMemberService.getAllProjectMembers(projectId)
        ));
    }

    @PreAuthorize("#principal.id.equals(#userId)")
    @PatchMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateProjectMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProjectMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Members updated successfully",
                projectMemberService.updateProjectMember(request, userId, projectId)
        ));
    }

    @PreAuthorize("#principal.id.equals(#userId)")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProjectMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectMemberService.deleteProjectMember(userId, projectId);

        return ResponseEntity.ok(new ApiResponse<>("Member removed successfully", null));
    }
}
