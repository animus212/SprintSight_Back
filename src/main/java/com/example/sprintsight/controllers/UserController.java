package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.UserRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.InvitationResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.responses.UserSummaryResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.security.JwtService;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.InvitationService;
import com.example.sprintsight.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private final JwtService jwtService;
    private final UserService userService;
    private final InvitationService invitationService;

    @GetMapping("/id/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("User retrieved successfully", userService.getUser(id)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>("User retrieved successfully", userService.getUserByUsername(username))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Put.class) @RequestBody UserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "User updated successfully",
                userService.updateUser(request, id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteUser(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtRefreshCookie().toString())
                .body(new ApiResponse<>("User deleted successfully", null));
    }

    @GetMapping("/{userId}/invitations")
    @PreAuthorize("#principal.id.equals(#userId)")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getPendingInvitations(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitations retrieved successfully",
                invitationService.getPendingInvitations(userId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> searchUsers(
            @RequestParam("username") String username,
            @RequestParam("projectId") UUID projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<UserSummaryResponse> results =
                userService.searchInvitableUsers(username, projectId, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Search completed", results));
    }
}
