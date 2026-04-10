package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Project retrieved successfully",
                projectService.getProject(id)
        ));
    }

    @GetMapping("/owned")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getOwnedProjects(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Projects retrieved successfully",
                projectService.getOwnedProjects(principal.getId())
        ));
    }

    @GetMapping("/member")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMemberProjects(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Projects retrieved successfully",
                projectService.getMemberProjects(principal.getId())
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> addProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Project created successfully",
                projectService.addProject(request, principal.getId())
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> putProject(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Put.class) @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Project updated successfully",
                projectService.updateProject(request, id, principal.getId(), true)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> patchProject(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Patch.class) @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Project updated successfully",
                projectService.updateProject(request, id, principal.getId(), false)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.deleteProject(id, principal.getId());

        return ResponseEntity.ok(new ApiResponse<>("Project deleted successfully", null));
    }
}
