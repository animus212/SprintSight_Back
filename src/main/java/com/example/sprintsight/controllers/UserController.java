package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.UpdateUserPatchRequest;
import com.example.sprintsight.dtos.requests.UpdateUserPutRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserPutRequest request
    ) {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        assert principal != null;
        if (!principal.getId().equals(id)) {
            throw new AccessDeniedException("You can only update your own account");
        }

        UserResponse updatedUser = userService.updateUserPut(request);

        return ResponseEntity.ok(new ApiResponse<>("User updated successfully", updatedUser));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserPatchRequest request
    ) {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        assert principal != null;
        if (!principal.getId().equals(id)) {
            throw new AccessDeniedException("You can only update your own account");
        }

        UserResponse updatedUser = userService.updateUserPatch(request);

        return ResponseEntity.ok(
                new ApiResponse<>("User updated successfully", updatedUser)
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        assert principal != null;
        if (!principal.getId().equals(id)) {
            throw new AccessDeniedException("You can only delete your own account");
        }

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
