package com.users.repository;

import com.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByAccountStatus(User.AccountStatus accountStatus);

    List<User> findByAccountLocked(boolean accountLocked);

    List<User> findByFailedLoginAttemptsGreaterThanEqual(int failedAttempts);

    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.accountLockTime < :unlockTime")
    List<User> findLockedUsersWhoseLockTimeHasExpired(@Param("unlockTime") LocalDateTime unlockTime);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :thresholdTime")
    List<User> findUsersWithUnverifiedEmailOlderThan(@Param("thresholdTime") LocalDateTime thresholdTime);

    @Query("SELECT u FROM User u WHERE u.phoneVerified = false AND u.createdAt < :thresholdTime")
    List<User> findUsersWithUnverifiedPhoneOlderThan(@Param("thresholdTime") LocalDateTime thresholdTime);

    @Query("SELECT u FROM User u WHERE u.accountStatus = :status AND u.lastLoginAt < :inactiveTime")
    List<User> findInactiveUsers(@Param("status") User.AccountStatus status, @Param("inactiveTime") LocalDateTime inactiveTime);

    Optional<User> findByEmailOrUsername(String email, String username);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :email OR u.phoneNumber = :email")
    Optional<User> findByEmailOrUsernameOrPhoneNumber(@Param("email") String email);
}
