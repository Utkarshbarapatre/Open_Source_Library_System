package com.buzzword.osls.service;

import com.buzzword.osls.model.RefreshToken;
import com.buzzword.osls.model.User;
import com.buzzword.osls.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationDays", 7L);
    }

    @Test
    public void testCreateRefreshToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(refreshTokenRepository.deleteByUser(user)).thenReturn(1);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(user);

        assertNotNull(token);
        assertEquals(user, token.getUser());
        assertNotNull(token.getToken());
        assertTrue(token.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    public void testFindByToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("my-token");

        when(refreshTokenRepository.findByToken("my-token")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("my-token");

        assertTrue(result.isPresent());
        assertEquals("my-token", result.get().getToken());
    }

    @Test
    public void testVerifyExpiry_Valid() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(3600));

        RefreshToken result = refreshTokenService.verifyExpiry(token);

        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    public void testVerifyExpiry_Expired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(3600));

        assertThrows(RuntimeException.class, () -> {
            refreshTokenService.verifyExpiry(token);
        }, "Refresh token has expired. Please sign in again.");

        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    public void testDeleteByUser() {
        User user = new User();
        user.setId(1L);

        when(refreshTokenRepository.deleteByUser(user)).thenReturn(1);

        refreshTokenService.deleteByUser(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }
}
