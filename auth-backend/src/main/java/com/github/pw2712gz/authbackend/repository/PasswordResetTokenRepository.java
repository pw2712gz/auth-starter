package com.github.pw2712gz.authbackend.repository;

import com.github.pw2712gz.authbackend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByExpiresAtBefore(Instant timestamp);
}
