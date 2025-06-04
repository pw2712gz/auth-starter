package com.github.pw2712gz.authbackend.exception;

import com.github.pw2712gz.authbackend.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Objects;

/**
 * Global exception handler for translating common exceptions into
 * consistent HTTP responses with structured error messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles login failures due to invalid credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    /**
     * Handles @Valid and form validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNullElseGet(
                ex.getBindingResult().getFieldError(),
                () -> new org.springframework.validation.FieldError("unknown", "unknown", "Validation failed")
        ).getDefaultMessage();

        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles validation exceptions for query params and path variables.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles business logic conflicts and illegal states.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Catches all uncaught runtime exceptions and maps them to 400 Bad Request.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return new ResponseEntity<>(
                new ErrorResponse(message, status.value(), Instant.now().toEpochMilli()),
                status
        );
    }
}
