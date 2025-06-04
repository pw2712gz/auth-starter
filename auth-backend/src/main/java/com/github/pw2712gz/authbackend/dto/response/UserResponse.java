package com.github.pw2712gz.authbackend.dto.response;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
