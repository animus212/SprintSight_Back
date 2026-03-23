package com.example.SprintSight.Services;

import com.example.SprintSight.Entities.RefreshToken;
import com.example.SprintSight.Entities.User;
import com.example.SprintSight.Exceptions.TokenRefreshException;
import com.example.SprintSight.Exceptions.UserNotFoundException;
import com.example.SprintSight.Repositories.RefreshTokenRepository;
import com.example.SprintSight.Repositories.UserRepository;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(DigestUtils.sha256Hex(rawToken));
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofDays(7)));

        refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for user: {}", userId);

        return rawToken;
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);

            log.warn("Expired refresh token presented by user: {}", token.getUser().getId());

            throw new TokenRefreshException("Refresh token expired. Please log in again.");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
