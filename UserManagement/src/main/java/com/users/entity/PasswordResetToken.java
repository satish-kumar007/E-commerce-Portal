package com.users.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    private static final int EXPIRATION_TIME_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private java.time.LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    public PasswordResetToken() {
        this.createdAt = java.time.LocalDateTime.now();
        this.expiryDate = this.createdAt.plusMinutes(EXPIRATION_TIME_MINUTES);
    }

    public PasswordResetToken(String token, User user) {
        this();
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public java.time.LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(java.time.LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return java.time.LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !isUsed() && !isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }
}
