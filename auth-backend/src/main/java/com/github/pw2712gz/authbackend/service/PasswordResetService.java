package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.entity.PasswordResetToken;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.PasswordResetTokenRepository;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for generating, validating, and cleaning up password reset tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final long TOKEN_EXPIRATION_SECONDS = 15 * 60;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${cleanup.token.min.age.days:1}")
    private int tokenMinAgeDays;

    @Getter
    @Setter
    @Value("${cleanup.token.interval.ms:3600000}")
    private long cleanupIntervalMs;

    /**
     * Creates a new password reset token for the given user.
     */
    public String createTokenForUser(User user) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(TOKEN_EXPIRATION_SECONDS))
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        log.debug("[PasswordReset] Token created for {}: {}", user.getEmail(), token);

        return token;
    }

    /**
     * Resets the user's password if the token is valid, unused, and unexpired.
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("[PasswordReset] Invalid token: {}", token);
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed()) {
            log.warn("[PasswordReset] Token already used: {}", token);
            return false;
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("[PasswordReset] Token expired: {}", token);
            tokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        if (user == null) {
            log.error("[PasswordReset] Token has no associated user.");
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        try {
            mailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.error("[PasswordReset] Failed to send confirmation email to {}: {}", user.getEmail(), e.getMessage(), e);
        }

        log.info("[PasswordReset] Password reset successful for {}", user.getEmail());
        return true;
    }

    /**
     * Deletes expired tokens that are older than the configured age.
     */
    public void cleanupExpiredTokens() {
        Instant cutoff = Instant.now().minus(tokenMinAgeDays, ChronoUnit.DAYS);
        List<PasswordResetToken> expired = tokenRepository.findAllByExpiresAtBefore(cutoff);

        if (!expired.isEmpty()) {
            tokenRepository.deleteAll(expired);
            log.info("[PasswordReset] Deleted {} expired token(s) older than {} day(s)", expired.size(), tokenMinAgeDays);
        } else {
            log.debug("[PasswordReset] No expired tokens found older than {} day(s)", tokenMinAgeDays);
        }
    }

    /**
     * Periodically cleans up expired password reset tokens.
     */
    @Scheduled(fixedRateString = "${cleanup.token.interval.ms}")
    public void scheduledTokenCleanup() {
        log.info("[PasswordReset] Scheduled token cleanup started");
        cleanupExpiredTokens();
    }
}
