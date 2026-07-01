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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testRegister_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@test.com");
        req.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = authService.register(req);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegister_UsernameTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("takenuser");

        when(userRepository.existsByUsername("takenuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.register(req);
        }, "Username already taken");
    }

    @Test
    public void testRegister_EmailTaken() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("taken@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.register(req);
        }, "Email already registered");
    }

    @Test
    public void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        JwtResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("USER", response.getRole());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testAdminLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("adminuser");
        req.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = new User();
        user.setId(2L);
        user.setUsername("adminuser");
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("adminuser")).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        JwtResponse response = authService.adminLogin(req);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    public void testAdminLogin_NotAdmin() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> {
            authService.adminLogin(req);
        }, "This account does not have admin access");
    }

    @Test
    public void testRefreshAccessToken_Success() {
        String tokenStr = "old-refresh-token";

        User user = new User();
        user.setUsername("testuser");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken(tokenStr);
        oldToken.setUser(user);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-refresh-token");

        when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.verifyExpiry(oldToken)).thenReturn(oldToken);
        when(jwtUtil.generateToken("testuser")).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

        TokenRefreshResponse response = authService.refreshAccessToken(tokenStr);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    public void testLogout() {
        String tokenStr = "refresh-token";
        User user = new User();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        when(refreshTokenService.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenService).deleteByUser(user);

        authService.logout(tokenStr);

        verify(refreshTokenService, times(1)).deleteByUser(user);
    }
}
