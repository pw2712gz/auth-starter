package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.entity.RefreshToken;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for managing refresh tokens — creation, validation, and cleanup.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenService {

    public static final long REFRESH_TOKEN_EXPIRY_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Generates and persists a new refresh token for the given user.
     */
    public String generateTokenForUser(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS));

        refreshTokenRepository.save(token);
        log.debug("[RefreshToken] Created token for user: {}", user.getEmail());

        return tokenValue;
    }

    /**
     * Validates a refresh token — must exist and not be expired.
     */
    public void validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("[RefreshToken] Invalid token");
                    return new IllegalArgumentException("Invalid refresh token");
                });

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("[RefreshToken] Expired token: deleting");
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalStateException("Refresh token expired");
        }

        log.debug("[RefreshToken] Token is valid");
    }

    /**
     * Deletes a refresh token by its token string.
     */
    public void delete(String token) {
        log.debug("[RefreshToken] Deleting token: {}", token);
        refreshTokenRepository.deleteByToken(token);
    }

    /**
     * Deletes all expired refresh tokens (runs hourly).
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("[RefreshToken] Cleanup: deleted {} expired token(s)", deleted);
        } else {
            log.debug("[RefreshToken] No expired tokens found during cleanup");
        }
    }
}
