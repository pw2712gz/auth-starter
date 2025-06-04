package com.github.pw2712gz.authbackend.dto.response;

public record ErrorResponse(
        String message,
        int status,
        long timestamp
) {
}
