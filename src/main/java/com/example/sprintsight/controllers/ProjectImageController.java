package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ImageUrlResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/projects/{id}/image", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectImageController {

    private final ProjectService projectService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUrlResponse>> uploadProjectImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        ImageUrlResponse result = projectService.updateProjectImage(id, file, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Project image updated successfully", result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteProjectImage(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProjectImage(id, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Project image removed successfully", null));
    }
}
