package com.users.service;

import com.users.entity.RefreshToken;
import com.users.entity.User;
import com.users.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String issueForUser(User user) {
        // Revoke older tokens optionally to limit concurrent sessions
        refreshTokenRepository.purgeExpired(LocalDateTime.now());
        String tokenValue = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        RefreshToken rt = new RefreshToken(tokenValue, user);
        refreshTokenRepository.save(rt);
        return tokenValue;
    }

    @Transactional(readOnly = true)
    public Optional<User> validateAndGetUser(String refreshToken) {
        Optional<RefreshToken> rtOpt = refreshTokenRepository.findByToken(refreshToken);
        if (rtOpt.isEmpty()) return Optional.empty();
        RefreshToken rt = rtOpt.get();
        if (!rt.isActive()) return Optional.empty();
        return Optional.of(rt.getUser());
    }

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllForUser(user);
    }
}
