package com.users.controller;

import com.users.dto.ProfileResponse;
import com.users.dto.ProfileUpdateRequest;
import com.users.entity.User;
import com.users.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Email update validation and duplicates
        if (StringUtils.hasText(req.getEmail())) {
            String emailLower = req.getEmail().toLowerCase(Locale.ROOT);
            if (!emailLower.equals(user.getEmail())) {
                if (userRepository.existsByEmail(emailLower)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
                user.setEmail(emailLower);
                user.setEmailVerified(false);
                user.setEmailVerifiedAt(null);
            }
        }

        // Phone update validation and duplicates
        if (StringUtils.hasText(req.getPhoneNumber())) {
            String newPhone = req.getPhoneNumber();
            if (user.getPhoneNumber() == null || !newPhone.equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(newPhone)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
                user.setPhoneNumber(newPhone);
                user.setPhoneVerified(false);
                user.setPhoneVerifiedAt(null);
            }
        }

        if (StringUtils.hasText(req.getFirstName())) {
            user.setFirstName(req.getFirstName());
        }
        if (StringUtils.hasText(req.getLastName())) {
            user.setLastName(req.getLastName());
        }
        if (StringUtils.hasText(req.getProfileImage())) {
            user.setProfileImage(req.getProfileImage());
        }

        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse res = new ProfileResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setProfileImage(user.getProfileImage());
        return res;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !StringUtils.hasText(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        String username = String.valueOf(auth.getPrincipal());
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }
}
