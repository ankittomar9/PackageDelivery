package com.company.authorizationservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject private field values normally provided by @Value in Spring
        ReflectionTestUtils.setField(jwtUtil, "secret", "defaultSuperSecretKeyForReturnOrderManagementSystem2026!");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1800000L);

        userDetails = new User("admin", "admin", Collections.emptyList());
    }

    @Test
    @DisplayName("Should generate a non-null JWT token for valid UserDetails")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should correctly extract username from valid token")
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("admin", extractedUsername);
    }

    @Test
    @DisplayName("Should return true for valid token")
    void testValidateToken_Success() {
        String token = jwtUtil.generateToken(userDetails);
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false for malformed token string")
    void testValidateToken_InvalidToken() {
        Boolean isValid = jwtUtil.validateToken("invalid.token.string");
        assertFalse(isValid);
    }
}