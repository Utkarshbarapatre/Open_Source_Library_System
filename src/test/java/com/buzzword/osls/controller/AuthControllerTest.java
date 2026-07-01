package com.buzzword.osls.controller;

import com.buzzword.osls.dto.*;
import com.buzzword.osls.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testRegister_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@test.com");
        req.setPassword("password");

        when(authService.register(any(RegisterRequest.class))).thenReturn("User registered successfully");

        ResponseEntity<?> response = authController.register(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse body = (ApiResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("User registered successfully", body.getMessage());
    }

    @Test
    public void testRegister_Failure() {
        RegisterRequest req = new RegisterRequest();

        when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Username already taken"));

        ResponseEntity<?> response = authController.register(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ApiResponse body = (ApiResponse) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Username already taken", body.getMessage());
    }

    @Test
    public void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password");

        JwtResponse jwtResponse = new JwtResponse("access-token", "refresh-token", 1L, "testuser", "test@test.com", "USER");
        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jwtResponse, response.getBody());
    }

    @Test
    public void testLogin_Failure() {
        LoginRequest req = new LoginRequest();

        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ApiResponse body = (ApiResponse) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Invalid credentials", body.getMessage());
    }

    @Test
    public void testAdminLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("adminuser");
        req.setPassword("password");

        JwtResponse jwtResponse = new JwtResponse("access-token", "refresh-token", 2L, "adminuser", "admin@test.com", "ADMIN");
        when(authService.adminLogin(any(LoginRequest.class))).thenReturn(jwtResponse);

        ResponseEntity<?> response = authController.adminLogin(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jwtResponse, response.getBody());
    }

    @Test
    public void testAdminLogin_Forbidden() {
        LoginRequest req = new LoginRequest();

        when(authService.adminLogin(any(LoginRequest.class))).thenThrow(new RuntimeException("Not an admin"));

        ResponseEntity<?> response = authController.adminLogin(req);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        ApiResponse body = (ApiResponse) response.getBody();
        assertFalse(body.isSuccess());
    }

    @Test
    public void testRefreshToken_Success() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("old-refresh-token");

        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse("new-access-token", "new-refresh-token");
        when(authService.refreshAccessToken("old-refresh-token")).thenReturn(tokenRefreshResponse);

        ResponseEntity<?> response = authController.refreshToken(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenRefreshResponse, response.getBody());
    }

    @Test
    public void testRefreshToken_Failure() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("invalid-token");

        when(authService.refreshAccessToken("invalid-token")).thenThrow(new RuntimeException("Invalid refresh token"));

        ResponseEntity<?> response = authController.refreshToken(req);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Invalid refresh token", body.getMessage());
    }

    @Test
    public void testLogout_Success() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("some-token");

        doNothing().when(authService).logout("some-token");

        ResponseEntity<?> response = authController.logout(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Logged out successfully", body.getMessage());
    }
}
