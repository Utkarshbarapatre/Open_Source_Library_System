package com.buzzword.osls.service;

import com.buzzword.osls.dto.JwtResponse;
import com.buzzword.osls.dto.LoginRequest;
import com.buzzword.osls.dto.RegisterRequest;
import com.buzzword.osls.dto.TokenRefreshResponse;
import com.buzzword.osls.model.RefreshToken;
import com.buzzword.osls.model.User;
import com.buzzword.osls.model.enums.Role;
import com.buzzword.osls.repository.UserRepository;
import com.buzzword.osls.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public String register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return "User registered successfully";
    }

    public JwtResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = userRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(req.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(),
            user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public JwtResponse adminLogin(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        User user = userRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("This account does not have admin access");
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        String accessToken = jwtUtil.generateToken(req.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken.getToken(),
            user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    /**
     * Validates the incoming refresh token, issues a new access token,
     * and rotates the refresh token (delete old → issue new).
     */
    public TokenRefreshResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiry(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getUsername());

        // Rotate: issue a new refresh token and revoke the old one
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }

    /**
     * Revokes the refresh token (logout).
     */
    public void logout(String refreshTokenStr) {
        refreshTokenService.findByToken(refreshTokenStr).ifPresent(rt ->
            refreshTokenService.deleteByUser(rt.getUser())
        );
    }
}
