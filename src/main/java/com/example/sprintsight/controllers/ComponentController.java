package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.ComponentRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.ComponentService;
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
@RequestMapping(value = "/api/projects/{projectId}/components",
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ComponentController {
    private final ComponentService componentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComponentResponse>>> getComponents(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Components retrieved successfully",
                componentService.getComponents(projectId, principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComponentResponse>> createComponent(
            @PathVariable UUID projectId,
            @Valid @RequestBody ComponentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ComponentResponse component = componentService.createComponent(
                request, projectId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Component created successfully", component));
    }

    @DeleteMapping("/{componentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(
            @PathVariable UUID projectId,
            @PathVariable UUID componentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        componentService.deleteComponent(componentId, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Component deleted successfully", null));
    }
}
