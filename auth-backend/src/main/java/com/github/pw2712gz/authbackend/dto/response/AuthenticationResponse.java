package com.github.pw2712gz.authbackend.dto.response;

import java.time.Instant;

public record AuthenticationResponse(
        String authenticationToken,
        String refreshToken,
        Instant expiresAt,
        String email
) {
}
