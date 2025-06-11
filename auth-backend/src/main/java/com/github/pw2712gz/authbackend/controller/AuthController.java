package com.github.pw2712gz.authbackend.controller;

import com.github.pw2712gz.authbackend.dto.request.*;
import com.github.pw2712gz.authbackend.dto.response.AuthenticationResponse;
import com.github.pw2712gz.authbackend.dto.response.MessageResponse;
import com.github.pw2712gz.authbackend.dto.response.UserResponse;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.service.AuthService;
import com.github.pw2712gz.authbackend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication and account-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("[Auth] Registering user: {}", request.email());
        authService.register(request);
        return ResponseEntity.ok(new MessageResponse("Registration successful"));
    }

    /**
     * Authenticates a user and returns a JWT + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("[Auth] Login requested: {}", request.email());
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an expired JWT using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        log.debug("[Auth] Refreshing token");
        AuthenticationResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the user and invalidates the refresh token.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new MessageResponse("Logged out and refresh token deleted"));
    }

    /**
     * Returns information about the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        ));
    }

    /**
     * Sends a password reset email with a token.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.sendResetPasswordEmail(request.email());
        return ResponseEntity.ok(new MessageResponse("Reset password email sent"));
    }

    /**
     * Resets the user's password using a valid reset token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        boolean success = passwordResetService.resetPassword(request.token(), request.newPassword());
        return success
                ? ResponseEntity.ok(new MessageResponse("Password successfully reset."))
                : ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token."));
    }

    /**
     * Public health check endpoint for load balancers or uptime monitors.
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("OK"));
    }
}
