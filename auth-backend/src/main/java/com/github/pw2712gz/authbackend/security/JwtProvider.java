package com.github.pw2712gz.authbackend.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for generating JWT access tokens using Spring's JwtEncoder.
 */
@Service
@Slf4j
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    @Getter
    private final Long jwtExpirationInMillis;

    public JwtProvider(JwtEncoder jwtEncoder,
                       @Value("${jwt.expiration.time}") Long jwtExpirationInMillis) {
        this.jwtEncoder = jwtEncoder;
        this.jwtExpirationInMillis = jwtExpirationInMillis;
    }

    /**
     * Generates a token using the authenticated principal's username.
     */
    public String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return generateTokenWithUsername(principal.getUsername());
    }

    /**
     * Generates a JWT token using the provided username.
     */
    public String generateTokenWithUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }

        if (jwtExpirationInMillis == null || jwtExpirationInMillis <= 0) {
            log.error("[JwtProvider] Invalid or missing jwt.expiration.time configuration");
            throw new IllegalStateException("JWT expiration time must be set in application.properties");
        }

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationInMillis);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-backend")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(username)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
