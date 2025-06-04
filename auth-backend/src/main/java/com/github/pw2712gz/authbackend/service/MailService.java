package com.github.pw2712gz.authbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Service for sending HTML emails using Thymeleaf and Resend API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpClient httpClient;

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends a Resend email with a rendered HTML template.
     */
    public void sendHtml(String to, String subject, String templateName, Map<String, Object> model) {
        if (to == null || subject == null || templateName == null || model == null) {
            throw new IllegalArgumentException("[MailService] Missing email parameters");
        }

        try {
            Context context = new Context();
            context.setVariables(model);
            String html = templateEngine.process(templateName, context);

            Map<String, Object> payload = Map.of(
                    "from", from,
                    "to", to,
                    "subject", subject,
                    "html", html
            );

            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.debug("[MailService] ✅ Sent email to '{}' | Subject: '{}' | Template: {}", to, subject, templateName);
            } else {
                log.error("[MailService] ❌ Failed to send email: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("Email failed with code: " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("[MailService] ❌ Exception sending email to '{}': {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendWelcomeEmail(String to, String name) {
        sendHtml(to, "🎉 Welcome to AuthApp", "email/welcome.html", Map.of("name", name));
    }

    public void sendResetPasswordEmail(String to, String name, String resetLink) {
        sendHtml(to, "Reset your password", "email/reset-password.html", Map.of("name", name, "link", resetLink));
    }

    public void sendPasswordChangedEmail(String to, String name) {
        sendHtml(to, "Your password was changed", "email/password-changed.html", Map.of("name", name));
    }
}