package com.github.pw2712gz.authbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email
) {
}
