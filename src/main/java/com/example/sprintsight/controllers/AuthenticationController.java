package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.LoginRequest;
import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.entities.RefreshToken;
import com.example.sprintsight.exceptions.TokenRefreshException;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.security.JwtService;
import com.example.sprintsight.services.RefreshTokenService;
import com.example.sprintsight.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserPrincipal userDetails = authenticate(request.username(), request.password());
        UserResponse userResponse = userService.getUser(userDetails.getId());

        return buildAuthResponse(userDetails, userResponse, "Logged in successfully");
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.addUser(request);
        UserPrincipal userDetails = authenticate(request.username(), request.password());

        return buildAuthResponse(userDetails, userResponse, "User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String rawToken = jwtService.getJwtRefreshFromCookies(request);

        if (rawToken != null) {
            RefreshToken refreshToken = refreshTokenService.findByToken(rawToken);

            refreshTokenService.deleteToken(refreshToken);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtService.getCleanJwtRefreshCookie().toString())
                .body(new ApiResponse<>("Logged out successfully", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest request) {
        String rawToken = jwtService.getJwtRefreshFromCookies(request);

        if (rawToken == null || rawToken.isEmpty()) {
            throw new TokenRefreshException("Refresh token is missing");
        }

        RefreshToken refreshToken = refreshTokenService.findByToken(rawToken);

        refreshTokenService.verifyExpiration(refreshToken);

        UserPrincipal userPrincipal = UserPrincipal.from(refreshToken.getUser());

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userPrincipal);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new ApiResponse<>("Token refreshed successfully", null));
    }

    private UserPrincipal authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (UserPrincipal) authentication.getPrincipal();
    }

    private ResponseEntity<ApiResponse<UserResponse>> buildAuthResponse(
            UserPrincipal userDetails,
            UserResponse userResponse,
            String message
    ) {
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);

        ResponseCookie jwtRefreshCookie = jwtService.generateRefreshJwtCookie(
                refreshTokenService.createRefreshToken(userDetails.getId())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new ApiResponse<>(message, userResponse));
    }
}
