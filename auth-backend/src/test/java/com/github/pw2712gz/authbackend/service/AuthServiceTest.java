package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.dto.request.LoginRequest;
import com.github.pw2712gz.authbackend.dto.request.RefreshTokenRequest;
import com.github.pw2712gz.authbackend.dto.request.RegisterRequest;
import com.github.pw2712gz.authbackend.dto.response.AuthenticationResponse;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import com.github.pw2712gz.authbackend.security.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private MailService mailService;
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthService authService;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("Should register user when email is unique")
    void register_success() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.register(request));
        verify(mailService).sendWelcomeEmail("john@example.com", "John");
    }

    @Test
    @DisplayName("Should throw when email is already registered")
    void register_emailExists() {
        RegisterRequest request = new RegisterRequest("Jane", "Smith", "jane@example.com", "password");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authService.register(request));
        assertEquals("Email is already registered", ex.getMessage());
    }

    @Test
    @DisplayName("Should return JWT and refresh token on login")
    void login_success() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        Authentication auth = mock(Authentication.class);
        User user = User.builder().email("test@example.com").build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtProvider.generateToken(auth)).thenReturn("jwt-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.generateTokenForUser(user)).thenReturn("refresh-token");
        when(jwtProvider.getJwtExpirationInMillis()).thenReturn(900_000L);

        AuthenticationResponse response = authService.login(request);

        assertEquals("jwt-token", response.authenticationToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("test@example.com", response.email());
        assertNotNull(response.expiresAt());
    }

    @Test
    @DisplayName("Should return new JWT on refresh")
    void refresh_success() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token", "user@example.com");

        when(jwtProvider.generateTokenWithUsername("user@example.com")).thenReturn("new-jwt");
        when(jwtProvider.getJwtExpirationInMillis()).thenReturn(900_000L);

        AuthenticationResponse response = authService.refresh(request);

        assertEquals("new-jwt", response.authenticationToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("user@example.com", response.email());
        assertNotNull(response.expiresAt());
    }

    @Test
    @DisplayName("Should delete refresh token on logout")
    void logout_success() {
        RefreshTokenRequest request = new RefreshTokenRequest("token123", "logout@example.com");

        authService.logout(request);

        verify(refreshTokenService).delete("token123");
    }

    @Test
    @DisplayName("Should not send reset email if user not found")
    void sendResetPasswordEmail_userNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        authService.sendResetPasswordEmail("missing@example.com");

        verify(passwordResetService, never()).createTokenForUser(any());
        verify(mailService, never()).sendResetPasswordEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should send reset email if user exists")
    void sendResetPasswordEmail_success() {
        User user = User.builder().email("reset@example.com").firstName("Reset").build();
        when(userRepository.findByEmail("reset@example.com")).thenReturn(Optional.of(user));
        when(passwordResetService.createTokenForUser(user)).thenReturn("token123");

        authService.sendResetPasswordEmail("reset@example.com");

        verify(mailService).sendResetPasswordEmail(eq("reset@example.com"), eq("Reset"), contains("token123"));
    }

    @Test
    @DisplayName("Should return user from SecurityContext")
    void getCurrentUser_success() {
        User user = User.builder().email("me@example.com").build();
        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("me@example.com");

        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));

        User result = authService.getCurrentUser();
        assertEquals("me@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw if user is not authenticated")
    void getCurrentUser_unauthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(IllegalStateException.class, () -> authService.getCurrentUser());
    }

    @Test
    @DisplayName("Should still register user even if welcome email fails")
    void register_emailSendFails() {
        RegisterRequest request = new RegisterRequest("Fail", "Mailer", "fail@example.com", "pass");
        when(userRepository.existsByEmail("fail@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP error")).when(mailService).sendWelcomeEmail(any(), any());

        assertDoesNotThrow(() -> authService.register(request));
        verify(mailService).sendWelcomeEmail("fail@example.com", "Fail");
    }

    @Test
    @DisplayName("Should throw if user email not found in DB")
    void getCurrentUser_emailNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("ghost@example.com");

        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authService.getCurrentUser());
        assertEquals("User not found: ghost@example.com", ex.getMessage());
    }
}
