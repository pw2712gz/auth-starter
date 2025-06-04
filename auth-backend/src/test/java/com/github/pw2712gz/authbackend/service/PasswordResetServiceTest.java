package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.entity.PasswordResetToken;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.PasswordResetTokenRepository;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailService mailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("createTokenForUser should generate and persist a new token")
    void createTokenForUser_shouldReturnTokenString() {
        User user = User.builder().email("test@example.com").build();
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);

        String token = passwordResetService.createTokenForUser(user);

        assertNotNull(token);
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isUsed());
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
    }

    @Test
    @DisplayName("resetPassword should return false if token does not exist")
    void resetPassword_shouldReturnFalseIfTokenNotFound() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        assertFalse(passwordResetService.resetPassword("invalid-token", "newpass"));
    }

    @Test
    @DisplayName("resetPassword should return false if token is already used")
    void resetPassword_shouldReturnFalseIfTokenIsUsed() {
        PasswordResetToken token = PasswordResetToken.builder().used(true).build();
        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));
        assertFalse(passwordResetService.resetPassword("used-token", "newpass"));
    }

    @Test
    @DisplayName("resetPassword should return false and delete token if expired")
    void resetPassword_shouldReturnFalseIfTokenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .used(false)
                .expiresAt(Instant.now().minusSeconds(10))
                .build();

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertFalse(passwordResetService.resetPassword("expired-token", "newpass"));
        verify(tokenRepository).delete(token);
    }

    @Test
    @DisplayName("resetPassword should return false if token user is null")
    void resetPassword_shouldReturnFalseIfTokenHasNoUser() {
        PasswordResetToken token = PasswordResetToken.builder()
                .used(false)
                .expiresAt(Instant.now().plusSeconds(60))
                .user(null)
                .build();

        when(tokenRepository.findByToken("no-user-token")).thenReturn(Optional.of(token));

        assertFalse(passwordResetService.resetPassword("no-user-token", "newpass"));
    }

    @Test
    @DisplayName("resetPassword should succeed if token is valid and send confirmation email")
    void resetPassword_shouldSucceedAndSendConfirmation() {
        User user = User.builder()
                .email("reset@example.com")
                .firstName("Reset")
                .build();

        PasswordResetToken token = PasswordResetToken.builder()
                .used(false)
                .expiresAt(Instant.now().plusSeconds(60))
                .user(user)
                .build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        boolean result = passwordResetService.resetPassword("valid-token", "newpass");

        assertTrue(result);
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
        verify(mailService).sendPasswordChangedEmail("reset@example.com", "Reset");
    }

    @Test
    @DisplayName("cleanupExpiredTokens should delete all expired tokens")
    void cleanupExpiredTokens_shouldDeleteExpiredTokens() {
        PasswordResetToken expired1 = PasswordResetToken.builder().id(1L).build();
        PasswordResetToken expired2 = PasswordResetToken.builder().id(2L).build();

        when(tokenRepository.findAllByExpiresAtBefore(any())).thenReturn(List.of(expired1, expired2));

        passwordResetService.cleanupExpiredTokens();

        verify(tokenRepository).deleteAll(List.of(expired1, expired2));
    }

    @Test
    @DisplayName("cleanupExpiredTokens should do nothing when no expired tokens exist")
    void cleanupExpiredTokens_shouldDoNothingIfNone() {
        when(tokenRepository.findAllByExpiresAtBefore(any())).thenReturn(List.of());

        passwordResetService.cleanupExpiredTokens();

        verify(tokenRepository).findAllByExpiresAtBefore(any());
        verifyNoMoreInteractions(tokenRepository); // ✅ prevents false assumptions
    }

}
