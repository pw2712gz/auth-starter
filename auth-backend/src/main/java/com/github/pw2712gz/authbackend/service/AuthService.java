package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.dto.request.LoginRequest;
import com.github.pw2712gz.authbackend.dto.request.RefreshTokenRequest;
import com.github.pw2712gz.authbackend.dto.request.RegisterRequest;
import com.github.pw2712gz.authbackend.dto.response.AuthenticationResponse;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import com.github.pw2712gz.authbackend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Service for handling authentication, registration, logout, and password reset flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final PasswordResetService passwordResetService;

    /**
     * Registers a new user and sends a welcome email.
     */
    public void register(RegisterRequest request) {
        log.info("[Auth] Registering new user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("[Auth] Registration failed — email already in use: {}", request.email());
            throw new IllegalStateException("Email is already registered");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        try {
            mailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.warn("[Auth] Welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns a new access and refresh token pair.
     */
    public AuthenticationResponse login(LoginRequest request) {
        log.info("[Auth] Login attempt for: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found: " + request.email()));

        String jwt = jwtProvider.generateToken(authentication);
        String refreshToken = refreshTokenService.generateTokenForUser(user);
        Instant expiration = Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis());

        log.info("[Auth] Login successful for: {}", request.email());

        return new AuthenticationResponse(jwt, refreshToken, expiration, request.email());
    }

    /**
     * Refreshes the access token using a valid refresh token.
     */
    public AuthenticationResponse refresh(RefreshTokenRequest request) {
        log.debug("[Auth] Refreshing token for: {}", request.email());

        refreshTokenService.validate(request.refreshToken());

        String jwt = jwtProvider.generateTokenWithUsername(request.email());
        Instant expiration = Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis());

        log.debug("[Auth] Token refreshed for: {}", request.email());

        return new AuthenticationResponse(jwt, request.refreshToken(), expiration, request.email());
    }

    /**
     * Invalidates a refresh token to log the user out.
     */
    public void logout(RefreshTokenRequest request) {
        log.debug("[Auth] Logging out: {}", request.email());
        refreshTokenService.delete(request.refreshToken());
        log.info("[Auth] Refresh token invalidated for: {}", request.email());
    }

    /**
     * Returns the currently authenticated user from the security context.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in context");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    /**
     * Sends a password reset email with a reset token if the user exists.
     */
    public void sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("[Auth] Password reset skipped — no user found for: {}", email);
            return;
        }

        User user = userOpt.get();
        String token = passwordResetService.createTokenForUser(user);
        String link = "http://localhost:4200/reset-password?token=" + token;

        try {
            mailService.sendResetPasswordEmail(user.getEmail(), user.getFirstName(), link);
            log.info("[Auth] Sent password reset email to: {}", email);
        } catch (Exception e) {
            log.error("[Auth] Failed to send reset email to {}: {}", email, e.getMessage(), e);
        }
    }
}
