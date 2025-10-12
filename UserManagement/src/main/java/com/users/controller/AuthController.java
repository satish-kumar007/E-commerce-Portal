package com.users.controller;

import com.users.dto.AuthResponse;
import com.users.dto.LoginRequest;
import com.users.dto.RegistrationRequest;
import com.users.dto.TokenResponse;
import com.users.dto.PasswordResetDtos;
import com.users.dto.RefreshRequest;
import com.users.entity.User;
import com.users.security.JwtService;
import com.users.service.AuthService;
import com.users.service.PasswordResetService;
import com.users.service.RefreshTokenService;
import com.users.service.VerificationService;
import com.users.service.UserService;
import com.users.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, AuthService authService, JwtService jwtService,
                          PasswordResetService passwordResetService, RefreshTokenService refreshTokenService,
                          VerificationService verificationService, UserRepository userRepository) {
        this.userService = userService;
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
        this.refreshTokenService = refreshTokenService;
        this.verificationService = verificationService;
        this.userRepository = userRepository;
    }
    //http://localhost:8082/api/auth/register
    // Registration endpoint (email or phone allowed). For social login, separate endpoints will be added.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse("Registration successful. Please verify your email/phone to activate the account.", user.getId(), user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String token = authService.login(request.getIdentifier(), request.getPassword(), ip, userAgent);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new TokenResponse(token, jwtService.getExpirationSeconds()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetDtos.ForgotResponse> forgotPassword(@Valid @RequestBody PasswordResetDtos.ForgotRequest request) {
        passwordResetService.initiateReset(request.getIdentifier());
        // In production: send token via email/SMS. Here we only return generic message for security.
        return ResponseEntity.ok(new PasswordResetDtos.ForgotResponse("If the account exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetDtos.ResetResponse> resetPassword(@Valid @RequestBody PasswordResetDtos.ResetRequest request) {
        boolean ok = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new PasswordResetDtos.ResetResponse("Invalid or expired reset token"));
        }
        return ResponseEntity.ok(new PasswordResetDtos.ResetResponse("Password has been reset successfully"));
    }

    @PostMapping("/login-with-refresh")
    public ResponseEntity<TokenResponse> loginWithRefresh(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String access = authService.login(request.getIdentifier(), request.getPassword(), ip, userAgent);
        if (access == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // load user to issue refresh token
        Optional<User> userOpt = userRepository.findByEmailOrUsernameOrPhoneNumber(request.getIdentifier());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String refresh = refreshTokenService.issueForUser(userOpt.get());
        return ResponseEntity.ok(new TokenResponse(access, jwtService.getExpirationSeconds(), refresh, 30L * 24 * 3600));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshTokenService.validateAndGetUser(request.getRefreshToken())
                .map(user -> ResponseEntity.ok(new TokenResponse(
                        jwtService.generateTokenWithTokenVersion(user.getUsername(), user.getTokenVersion()),
                        jwtService.getExpirationSeconds(),
                        request.getRefreshToken(),
                        30L * 24 * 3600
                )))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !StringUtils.hasText(String.valueOf(auth.getPrincipal()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = String.valueOf(auth.getPrincipal());
        Optional<User> user = userRepository.findByEmailOrUsernameOrPhoneNumber(username);
        user.ifPresent(refreshTokenService::revokeAllForUser);
        return ResponseEntity.noContent().build();
    }

    // Verification: send for authenticated user (email or phone)
    @PostMapping("/verify/send")
    public ResponseEntity<AuthResponse> sendVerification(@RequestParam("type") String type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !StringUtils.hasText(String.valueOf(auth.getPrincipal()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = String.valueOf(auth.getPrincipal());
        Optional<User> userOpt = userRepository.findByEmailOrUsernameOrPhoneNumber(username);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userOpt.get();
        com.users.entity.VerificationToken.Type t = "phone".equalsIgnoreCase(type)
                ? com.users.entity.VerificationToken.Type.PHONE
                : com.users.entity.VerificationToken.Type.EMAIL;
        verificationService.sendVerification(user, t);
        return ResponseEntity.ok(new AuthResponse("Verification sent.", user.getId(), user.getUsername()));
    }

    @PostMapping("/verify/confirm")
    public ResponseEntity<AuthResponse> confirmVerification(@RequestParam("token") String token) {
        boolean ok = verificationService.verify(token);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse("Invalid or expired verification token", null, null));
        return ResponseEntity.ok(new AuthResponse("Verification successful.", null, null));
    }

    // Helper methods removed; using UserRepository directly for lookups.
}
