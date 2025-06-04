package com.github.pw2712gz.authbackend.service;

import com.github.pw2712gz.authbackend.entity.RefreshToken;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encoded-pwd")
                .enabled(true)
                .createdAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("generateTokenForUser should create and save a refresh token associated with user")
    void generateTokenForUser_shouldCreateAndSaveToken() {
        String token = refreshTokenService.generateTokenForUser(mockUser);

        assertNotNull(token);
        verify(refreshTokenRepository).save(argThat(savedToken ->
                savedToken.getUser().equals(mockUser) &&
                        savedToken.getToken() != null &&
                        savedToken.getCreatedAt() != null &&
                        savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt())
        ));
    }

    @Test
    @DisplayName("validate should succeed for valid non-expired token")
    void validate_shouldSucceedIfTokenIsValidAndNotExpired() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setExpiresAt(Instant.now().plusSeconds(60));

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> refreshTokenService.validate(tokenValue));
    }

    @Test
    @DisplayName("validate should throw if token does not exist")
    void validate_shouldThrowIfTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> refreshTokenService.validate("bad-token"));

        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    @DisplayName("validate should throw and delete if token is expired")
    void validate_shouldThrowIfTokenIsExpired() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(tokenValue);
        expiredToken.setExpiresAt(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        Exception ex = assertThrows(IllegalStateException.class,
                () -> refreshTokenService.validate(tokenValue));

        assertEquals("Refresh token expired", ex.getMessage());
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("delete should remove token by value")
    void delete_shouldDeleteTokenByValue() {
        refreshTokenService.delete("some-token");
        verify(refreshTokenRepository).deleteByToken("some-token");
    }

    @Test
    @DisplayName("cleanupExpiredTokens should delete expired tokens and log if count > 0")
    void cleanupExpiredTokens_shouldLogWhenTokensDeleted() {
        when(refreshTokenRepository.deleteAllByExpiresAtBefore(any())).thenReturn(2);

        refreshTokenService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteAllByExpiresAtBefore(any());
    }

    @Test
    @DisplayName("cleanupExpiredTokens should still run if no tokens were deleted")
    void cleanupExpiredTokens_shouldNotLogWhenNoTokensDeleted() {
        when(refreshTokenRepository.deleteAllByExpiresAtBefore(any())).thenReturn(0);

        refreshTokenService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteAllByExpiresAtBefore(any());
    }
}
