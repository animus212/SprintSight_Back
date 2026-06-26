package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ImageUrlResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users/{id}/profile-picture", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserProfilePictureController {
    private final UserService userService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<ImageUrlResponse>> uploadProfilePicture(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        ImageUrlResponse result = userService.updateProfilePicture(id, file);
        return ResponseEntity.ok(new ApiResponse<>("Profile picture updated successfully", result));
    }

    @DeleteMapping
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePicture(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteProfilePicture(id);
        return ResponseEntity.ok(new ApiResponse<>("Profile picture removed successfully", null));
    }
}
