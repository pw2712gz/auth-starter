package com.github.pw2712gz.authbackend.repository;

import com.github.pw2712gz.authbackend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    int deleteAllByExpiresAtBefore(Instant time);

    boolean existsByToken(String token);
}
