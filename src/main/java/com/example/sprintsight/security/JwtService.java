package com.example.sprintsight.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${SprintSight.app.jwtSecret}")
    private String jwtSecret;

    @Value("${SprintSight.app.jwtExpirationMs:900000}")
    private long jwtExpirationMs;

    @Value("${SprintSight.app.refreshExpirationDays:7}")
    private long refreshExpirationDays;

    private static final String JWT_COOKIE = "SprintSightJwtCookie";
    private static final String JWT_REFRESH_COOKIE = "SprintSightJwtRefreshCookie";

    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateJwtToken(userDetails.getUsername());

        return generateCookie(JWT_COOKIE, jwt, "/api", jwtExpirationMs / 1000);
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return generateCookie(JWT_REFRESH_COOKIE, refreshToken, "/api/auth",
                refreshExpirationDays * 24 * 60 * 60);
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, JWT_COOKIE);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request, JWT_REFRESH_COOKIE);
    }

    public ResponseCookie getCleanJwtCookie() {
        return generateCookie(JWT_COOKIE, "", "/api", 0);
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        return generateCookie(JWT_REFRESH_COOKIE, "", "/api/auth", 0);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateJwtToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return userDetails.getUsername().equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private  String generateJwtToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Base64.getUrlDecoder().decode(jwtSecret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes after Base64 decoding. " +
                            "Generate a new one with: openssl rand -base64 64");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private ResponseCookie generateCookie(String name, String value, String path, long maxAge) {
        return ResponseCookie.from(name, value)
                .path(path)
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);

        return cookie != null ? cookie.getValue() : null;
    }
}
