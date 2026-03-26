package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable UUID id) {
        ProjectResponse projectResponse = projectService.getProject(id);

        return ResponseEntity.ok(new ApiResponse<>("Project retrieved successfully", projectResponse));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects() {
        List<ProjectResponse> projectResponse = projectService.getProjects();

        return ResponseEntity.ok(new ApiResponse<>("Projects retrieved successfully", projectResponse));
    }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> addProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse projectResponse = projectService.addProject(request);

        return ResponseEntity.ok(new ApiResponse<>("Project created successfully", projectResponse));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> putProject(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Put.class) @RequestBody UpdateProjectRequest request
    ) {
        ProjectResponse updatedProject = projectService.updateProject(request, id, true);

        return ResponseEntity.ok(new ApiResponse<>("Project updated successfully", updatedProject));
    }

    @PatchMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> patchProject(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Patch.class) @RequestBody UpdateProjectRequest request
    ) {
        ProjectResponse updatedProject = projectService.updateProject(request, id, false);

        return ResponseEntity.ok(new ApiResponse<>("Project updated successfully", updatedProject));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
}
