package com.users.service;

import com.users.entity.PasswordResetToken;
import com.users.entity.User;
import com.users.repository.PasswordResetTokenRepository;
import com.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String initiateReset(String identifier) {
        Optional<User> userOpt = userRepository.findByEmailOrUsernameOrPhoneNumber(identifier);
        if (userOpt.isEmpty()) {
            // Do not reveal that the user doesn't exist.
            return null;
        }
        User user = userOpt.get();

        // Optionally invalidate existing active token
        tokenRepository.findActiveTokenForUser(user, LocalDateTime.now()).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });

        // Create new token
        String tokenValue = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        PasswordResetToken token = new PasswordResetToken(tokenValue, user);
        tokenRepository.save(token);
        return tokenValue;
    }

    @Transactional
    public boolean resetPassword(String tokenValue, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (!token.isValid()) return false;

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        // Invalidate all active sessions by bumping tokenVersion
        user.incrementTokenVersion();
        userRepository.save(user);

        token.markAsUsed();
        tokenRepository.save(token);
        return true;
    }
}
