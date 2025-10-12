package com.users.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean successful;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private java.time.LocalDateTime attemptTime;

    @Column(length = 1000)
    private String failureReason;

    public LoginAttempt() {
        this.attemptTime = java.time.LocalDateTime.now();
    }

    public LoginAttempt(User user, boolean successful, String ipAddress, String userAgent) {
        this();
        this.user = user;
        this.successful = successful;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public LoginAttempt(User user, boolean successful, String ipAddress, String userAgent, String failureReason) {
        this(user, successful, ipAddress, userAgent);
        this.failureReason = failureReason;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public java.time.LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    public void setAttemptTime(java.time.LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
