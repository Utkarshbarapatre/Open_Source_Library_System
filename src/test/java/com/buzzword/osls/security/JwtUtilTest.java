package com.buzzword.osls.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "OSLSBuzzwordSecretKey2024SuperLongSecureSecretKeyForJWTSigning!@#");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 900000);
    }

    @Test
    public void testGenerateAndValidateToken() {
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtUtil.generateToken(username);
        assertNotNull(token);

        assertTrue(jwtUtil.validateToken(token, userDetails));
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
    }

    @Test
    public void testValidateTokenInvalidUser() {
        String username = "testuser";
        UserDetails otherUserDetails = new User("otheruser", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(username);
        assertFalse(jwtUtil.validateToken(token, otherUserDetails));
    }

    @Test
    public void testExpiredToken() {
        // Set short expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000);
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtUtil.generateToken(username);
        assertFalse(jwtUtil.validateToken(token, userDetails));
    }
}
