package com.users.service;

import com.users.entity.LoginAttempt;
import com.users.entity.User;
import com.users.repository.LoginAttemptRepository;
import com.users.repository.UserRepository;
import com.users.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_MINUTES = 15;

    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       LoginAttemptRepository loginAttemptRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String login(String identifier, String password, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByEmailOrUsernameOrPhoneNumber(identifier);
        if (userOpt.isEmpty()) {
            // Do not reveal existence
            return null;
        }
        User user = userOpt.get();

        // If account locked, check if lock duration expired
        if (user.isAccountLocked()) {
            if (user.getAccountLockTime() != null && user.getAccountLockTime().plusMinutes(ACCOUNT_LOCK_MINUTES).isBefore(LocalDateTime.now())) {
                user.unlockAccount();
                userRepository.save(user);
            } else {
                loginAttemptRepository.save(new LoginAttempt(user, false, ipAddress, userAgent, "Account locked"));
                return null;
            }
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            user.incrementFailedLoginAttempts();
            loginAttemptRepository.save(new LoginAttempt(user, false, ipAddress, userAgent, "Invalid credentials"));
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
            }
            userRepository.save(user);
            return null;
        }

        // Successful login
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        loginAttemptRepository.save(new LoginAttempt(user, true, ipAddress, userAgent));

        return jwtService.generateTokenWithTokenVersion(user.getUsername(), user.getTokenVersion());
    }
}
