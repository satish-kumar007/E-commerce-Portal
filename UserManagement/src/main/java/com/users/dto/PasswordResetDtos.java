package com.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDtos {
    public static class ForgotRequest {
        @NotBlank(message = "Identifier (email/username/phone) is required")
        private String identifier;
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }

    public static class ForgotResponse {
        private String message;
        public ForgotResponse() {}
        public ForgotResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ResetRequest {
        @NotBlank(message = "Reset token is required")
        private String token;
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String newPassword;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class ResetResponse {
        private String message;
        public ResetResponse() {}
        public ResetResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
