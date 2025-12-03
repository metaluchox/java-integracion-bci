package com.bci.userregistration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // HS512 requires at least 512 bits (64 bytes) - this is 64+ characters
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-jwt-token-generation-and-validation-with-enough-length-for-hs512");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtService.generateToken(email, userId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(email, userId);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        // Arrange
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(email, userId);

        // Act
        String extractedEmail = jwtService.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }
}
