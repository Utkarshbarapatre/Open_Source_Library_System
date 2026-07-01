package com.buzzword.osls.controller;

import com.buzzword.osls.dto.ApiResponse;
import com.buzzword.osls.dto.JwtResponse;
import com.buzzword.osls.dto.LoginRequest;
import com.buzzword.osls.dto.RefreshRequest;
import com.buzzword.osls.dto.RegisterRequest;
import com.buzzword.osls.dto.TokenRefreshResponse;
import com.buzzword.osls.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            String msg = authService.register(req);
            return ResponseEntity.ok(new ApiResponse(true, msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            JwtResponse jwt = authService.login(req);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid credentials"));
        }
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest req) {
        try {
            JwtResponse jwt = authService.adminLogin(req);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Invalid credentials or not an admin account"));
        }
    }

    /**
     * Exchange a valid refresh token for a new access token + rotated refresh token.
     * POST /api/auth/refresh   { "refreshToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest req) {
        try {
            TokenRefreshResponse response = authService.refreshAccessToken(req.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Revoke the refresh token (logout).
     * POST /api/auth/logout   { "refreshToken": "..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
}
