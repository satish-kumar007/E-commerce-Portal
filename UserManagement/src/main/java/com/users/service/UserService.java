package com.users.service;

import com.users.dto.RegistrationRequest;
import com.users.entity.User;
import com.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationRequest request) {
        // Basic input rules: email or phone must be present
        if (!StringUtils.hasText(request.getEmail()) && !StringUtils.hasText(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Either email or phone number must be provided");
        }

        // Duplicate checks
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail().toLowerCase(Locale.ROOT))) {
            throw new DuplicateFieldException("email", "Email already in use");
        }
        if (StringUtils.hasText(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateFieldException("phoneNumber", "Phone number already in use");
        }
        String username = request.getUsername();
        if (!StringUtils.hasText(username)) {
            username = deriveUsername(request);
        }
        if (userRepository.existsByUsername(username)) {
            // If derived username collides, append a short random suffix
            username = username + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        User user = new User();
        user.setUsername(username);
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().toLowerCase(Locale.ROOT));
        } else {
            // Generate a placeholder email if not provided (optional depending on business rules)
            user.setEmail("user-" + UUID.randomUUID() + "@placeholder.local");
        }
        user.setPhoneNumber(StringUtils.hasText(request.getPhoneNumber()) ? request.getPhoneNumber() : null);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(User.AccountStatus.PENDING);
        user.setAccountType(User.AccountType.LOCAL);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setAccountLocked(false);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Handle race conditions for duplicates due to concurrent registration
            throw new DuplicateFieldException("emailOrPhone", "Duplicate email/phone detected");
        }
    }

    private String deriveUsername(RegistrationRequest request) {
        if (StringUtils.hasText(request.getUsername())) {
            return request.getUsername();
        }
        if (StringUtils.hasText(request.getEmail())) {
            String local = request.getEmail().split("@")[0];
            return local.replaceAll("[^A-Za-z0-9_.-]", "");
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            return "user" + request.getPhoneNumber().replaceAll("[^0-9]", "");
        }
        return "user" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static class DuplicateFieldException extends RuntimeException {
        private final String field;
        public DuplicateFieldException(String field, String message) {
            super(message);
            this.field = field;
        }
        public String getField() { return field; }
    }
}
