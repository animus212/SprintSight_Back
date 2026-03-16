package com.example.SprintSight.Controllers;

import com.example.SprintSight.DTOs.ApiResponse;
import com.example.SprintSight.DTOs.LoginDTO;
import com.example.SprintSight.DTOs.UserDTO;
import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Services.JWTService;
import com.example.SprintSight.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<String>> addUser(@Valid @RequestBody User user) {
        user = userService.AddUser(user);

        String jwtToken = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(
                new ApiResponse<>("User added", jwtToken)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        String jwtToken = jwtService.generateToken(loginDTO.getUsername());

        return ResponseEntity.ok(
                new ApiResponse<>("Login successful", jwtToken)
        );
    }

    @PutMapping("/users")
    public ResponseEntity<ApiResponse<User>> updateUser(@Valid @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(userDTO);

        return ResponseEntity.ok(
                new ApiResponse<>("User updated", updatedUser)
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable @Valid @RequestParam UUID id){
        userService.deleteUser(id);

        return ResponseEntity.ok(
                new ApiResponse<>("User deleted", null)
        );
    }
}
