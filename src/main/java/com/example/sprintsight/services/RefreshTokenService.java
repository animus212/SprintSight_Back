package com.example.sprintsight.services;

import com.example.sprintsight.entities.RefreshToken;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.TokenRefreshException;
import com.example.sprintsight.exceptions.EntityNotFoundException;
import com.example.sprintsight.repositories.RefreshTokenRepository;
import com.example.sprintsight.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String rawToken) {
        return refreshTokenRepository.findByToken(DigestUtils.sha256Hex(rawToken))
                .orElseThrow(() -> new TokenRefreshException("Invalid or expired refresh token"));
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(DigestUtils.sha256Hex(rawToken));
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofDays(7)));
        refreshToken.setCreatedAt(Instant.now());

        refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for user: {}", userId);

        return rawToken;
    }

    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);

            log.warn("Expired refresh token presented by user: {}", token.getUser().getId());

            throw new TokenRefreshException("Refresh token expired. Please log in again.");
        }
    }

    @Transactional
    public void deleteToken(RefreshToken token) {
        refreshTokenRepository.delete(token);

        log.info("Deleted refresh token for user: {}", token.getUser().getId());
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
