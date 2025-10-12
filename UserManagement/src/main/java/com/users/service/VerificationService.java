package com.users.service;

import com.users.entity.User;
import com.users.entity.VerificationToken;
import com.users.repository.UserRepository;
import com.users.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerificationService(VerificationTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String sendVerification(User user, VerificationToken.Type type) {
        tokenRepository.findActiveToken(user, type, LocalDateTime.now()).ifPresent(vt -> {
            vt.setUsed(true);
            tokenRepository.save(vt);
        });
        String tokenValue = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        VerificationToken token = new VerificationToken(tokenValue, type, user);
        tokenRepository.save(token);
        // TODO: Integrate with Email/SMS provider to send the token link/code.
        return tokenValue;
    }

    @Transactional
    public boolean verify(String tokenValue) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) return false;
        VerificationToken token = tokenOpt.get();
        if (!token.isValid()) return false;
        User user = token.getUser();
        if (token.getType() == VerificationToken.Type.EMAIL) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
        } else if (token.getType() == VerificationToken.Type.PHONE) {
            user.setPhoneVerified(true);
            user.setPhoneVerifiedAt(LocalDateTime.now());
        }
        // Activate account if any verification policy met (email or phone)
        if (user.isEmailVerified() || user.isPhoneVerified()) {
            user.setAccountStatus(User.AccountStatus.ACTIVE);
        }
        userRepository.save(user);
        token.markAsUsed();
        tokenRepository.save(token);
        return true;
    }
}
