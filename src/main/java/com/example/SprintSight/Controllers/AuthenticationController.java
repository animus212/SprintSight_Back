package com.example.SprintSight.Controllers;

import com.example.SprintSight.Payloads.Requests.LoginRequest;
import com.example.SprintSight.Payloads.Requests.RegisterRequest;
import com.example.SprintSight.Payloads.Responses.ApiResponse;
import com.example.SprintSight.Entities.RefreshToken;
import com.example.SprintSight.Exceptions.TokenRefreshException;
import com.example.SprintSight.Payloads.Responses.UserResponse;
import com.example.SprintSight.Security.UserPrincipal;
import com.example.SprintSight.Services.JwtService;
import com.example.SprintSight.Services.RefreshTokenService;
import com.example.SprintSight.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        assert userDetails != null;
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);

        ResponseCookie jwtRefreshCookie = jwtService.generateRefreshJwtCookie(
                refreshTokenService.createRefreshToken(userDetails.getId())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new ApiResponse<>("Logged in successfully", null));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.addUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("User registered successfully", userResponse));
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
}
