package com.github.pw2712gz.authbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pw2712gz.authbackend.dto.request.ForgotPasswordRequest;
import com.github.pw2712gz.authbackend.dto.request.LoginRequest;
import com.github.pw2712gz.authbackend.dto.request.RefreshTokenRequest;
import com.github.pw2712gz.authbackend.dto.request.RegisterRequest;
import com.github.pw2712gz.authbackend.entity.User;
import com.github.pw2712gz.authbackend.repository.RefreshTokenRepository;
import com.github.pw2712gz.authbackend.repository.UserRepository;
import com.github.pw2712gz.authbackend.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private final String email = "auth@test.com";
    private final String password = "Password123";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User user = User.builder()
                .firstName("Auth")
                .lastName("Tester")
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
    }

    @Test
    void register_shouldSucceed() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john.doe@example.com", "Test12345");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldSucceed() throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_shouldSucceed() throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow();
        String refreshToken = refreshTokenService.generateTokenForUser(user);
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken, email);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticationToken").exists());
    }

    @Test
    void getCurrentUser_shouldReturnUserInfo() throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        String token = jsonNode.get("authenticationToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void sendResetPasswordEmail_shouldSendIfUserExists() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void sendResetPasswordEmail_shouldSkipIfUserMissing() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void logout_shouldInvalidateToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        String accessToken = jsonNode.get("authenticationToken").asText();
        String refreshToken = jsonNode.get("refreshToken").asText();

        RefreshTokenRequest logoutRequest = new RefreshTokenRequest(refreshToken, email);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.existsByToken(refreshToken)).isFalse();
    }
}
