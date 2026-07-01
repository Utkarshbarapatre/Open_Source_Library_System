package com.buzzword.osls.service;

import com.buzzword.osls.model.RefreshToken;
import com.buzzword.osls.model.User;
import com.buzzword.osls.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.refresh.expiration-days:7}")
    private long refreshExpirationDays;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Creates (or replaces) a refresh token for the given user.
     * Old token is deleted first so each user has at most one active refresh token.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Remove any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshExpirationDays * 24 * 60 * 60));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Looks up a refresh token by its string value.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verifies the token is not expired. If expired, deletes it and throws.
     */
    @Transactional
    public RefreshToken verifyExpiry(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please sign in again.");
        }
        return token;
    }

    /**
     * Deletes refresh token for the given user (logout).
     */
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
