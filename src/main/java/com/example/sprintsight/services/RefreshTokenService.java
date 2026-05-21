package com.example.sprintsight.services;

import com.example.sprintsight.entities.RefreshToken;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.TokenRefreshException;
import com.example.sprintsight.repositories.RefreshTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;

    @Value("${SprintSight.app.refreshExpirationDays:7}")
    private long refreshExpirationDays;

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String rawToken) {
        return refreshTokenRepository.findByToken(DigestUtils.sha256Hex(rawToken))
                .orElseThrow(() -> new TokenRefreshException("Invalid or expired refresh token"));
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        User userRef = entityManager.getReference(User.class, userId);

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRef);
        refreshToken.setToken(DigestUtils.sha256Hex(rawToken));
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofDays(refreshExpirationDays)));

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
        int deleted = refreshTokenRepository.deleteByUser_Id(userId);

        log.info("Deleted {} refresh token(s) for user: {}", deleted, userId);
    }

    @Scheduled(cron = "${sprintsight.refresh-token.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void deleteExpiredTokens() {
        int deleted = refreshTokenRepository.deleteAllExpired(Instant.now());

        if (deleted > 0) {
            log.info("Expired refresh token cleanup: deleted {} token(s)", deleted);
        }
    }
}
