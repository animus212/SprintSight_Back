package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.security.JwtService;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>("User retrieved successfully", userService.getUser(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> putUser(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Put.class) @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return updateUser(id, request, principal, true);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#principal.id.equals(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Patch.class) @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return updateUser(id, request, principal, false);
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

    private ResponseEntity<ApiResponse<UserResponse>> updateUser(
            UUID id,
            UpdateUserRequest request,
            UserPrincipal principal,
            boolean isPut
    ) {
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(principal);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new ApiResponse<>(
                        "User updated successfully",
                        userService.updateUser(request, id, isPut)
                ));
    }
}
