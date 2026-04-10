package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("#id == authentication.principal.id")
public class UserController {
    private final UserService userService;

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> putUser(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Put.class) @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(request, id, true);

        return ResponseEntity.ok(new ApiResponse<>("User updated successfully", updatedUser));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> patchUser(
            @PathVariable UUID id,
            @Validated(ValidationGroups.Patch.class) @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(request, id, false);

        return ResponseEntity.ok(new ApiResponse<>("User updated successfully", updatedUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);

        return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", null));
    }
}
