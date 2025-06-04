package com.github.pw2712gz.authbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private TemplateEngine templateEngine;
    private HttpClient mockHttpClient;
    private MailService mailService;

    @BeforeEach
    void setUp() throws Exception {
        templateEngine = mock(TemplateEngine.class);
        mockHttpClient = mock(HttpClient.class);

        mailService = new MailService(templateEngine);

        // Inject private fields using reflection
        setPrivateField(mailService, "httpClient", mockHttpClient);
        setPrivateField(mailService, "apiKey", "test-api-key");
        setPrivateField(mailService, "from", "Auth App <auth@auth.ayubyusuf.dev>");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for missing parameters")
    void shouldThrowIllegalArgumentExceptionIfMissingParams() {
        assertThatThrownBy(() -> mailService.sendHtml(null, "Subject", "template", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mailService.sendHtml("to@example.com", null, "template", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mailService.sendHtml("to@example.com", "Subject", null, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mailService.sendHtml("to@example.com", "Subject", "template", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should render template and send HTTP request")
    void shouldRenderTemplateAndSendHttpRequest() throws Exception {
        when(templateEngine.process(eq("email/welcome.html"), any(Context.class)))
                .thenReturn("<html>Email</html>");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);

        when(mockHttpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(mockResponse);

        mailService.sendHtml("to@example.com", "Subject", "email/welcome.html", Map.of("name", "Ayub"));

        verify(templateEngine).process(eq("email/welcome.html"), any(Context.class));
        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException on HTTP failure")
    void shouldThrowRuntimeExceptionOnHttpFailure() throws Exception {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Error</html>");

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");

        when(mockHttpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(mockResponse);

        assertThatThrownBy(() -> mailService.sendHtml(
                "to@example.com",
                "Subject",
                "email/template.html",
                Map.of("name", "Ayub"))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");
    }
}