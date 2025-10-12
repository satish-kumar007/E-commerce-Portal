package com.users.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    private static final int EXPIRATION_DAYS = 30;

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
    private boolean revoked = false;

    @Column(nullable = false)
    private java.time.LocalDateTime createdAt;

    public RefreshToken() {
        this.createdAt = java.time.LocalDateTime.now();
        this.expiryDate = this.createdAt.plusDays(EXPIRATION_DAYS);
    }

    public RefreshToken(String token, User user) {
        this();
        this.token = token;
        this.user = user;
    }

    public boolean isExpired() { return java.time.LocalDateTime.now().isAfter(expiryDate); }
    public boolean isActive() { return !revoked && !isExpired(); }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public java.time.LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(java.time.LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
