package com.github.pw2712gz.authbackend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class JwtProviderTest {

    private JwtEncoder jwtEncoder;
    private JwtProvider jwtProvider;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jwtEncoder = mock(JwtEncoder.class);
        jwtProvider = new JwtProvider(jwtEncoder, 900_000L); // 15 min
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("Returns token when username is valid")
    void generateTokenWithUsername_returnsToken() {
        String username = "test@example.com";
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = jwtProvider.generateTokenWithUsername(username);

        assertEquals("mock-token", token);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("Throws if expiration is zero")
    void generateTokenWithUsername_throwsIfExpirationMissing() {
        JwtProvider invalidProvider = new JwtProvider(jwtEncoder, 0L);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> invalidProvider.generateTokenWithUsername("bad@example.com")
        );

        assertEquals("JWT expiration time must be set in application.properties", ex.getMessage());
    }

    @Test
    @DisplayName("Generates token from Authentication principal")
    void generateToken_usesAuthentication() {
        Authentication auth = mock(Authentication.class);
        User principal = new User("user@example.com", "password", Collections.emptyList());
        when(auth.getPrincipal()).thenReturn(principal);

        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("auth-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        String token = jwtProvider.generateToken(auth);

        assertEquals("auth-token", token);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("Returns configured expiration time")
    void getJwtExpirationInMillis_returnsExpected() {
        assertEquals(900_000L, jwtProvider.getJwtExpirationInMillis());
    }

    @Test
    @DisplayName("Throws if username is null")
    void generateTokenWithUsername_throwsIfUsernameNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> jwtProvider.generateTokenWithUsername(null)
        );
        assertEquals("Username must not be null", ex.getMessage());
    }
}
