package com.users.repository;

import com.users.entity.LoginAttempt;
import com.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByUser(User user);

    List<LoginAttempt> findByUserId(Long userId);

    List<LoginAttempt> findByUserAndSuccessful(User user, boolean successful);

    List<LoginAttempt> findByUserIdAndSuccessful(Long userId, boolean successful);

    List<LoginAttempt> findByUserAndAttemptTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);

    List<LoginAttempt> findByUserIdAndAttemptTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.user = :user AND la.successful = false AND la.attemptTime > :sinceTime")
    long countFailedLoginAttemptsSince(@Param("user") User user, @Param("sinceTime") LocalDateTime sinceTime);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.user.id = :userId AND la.successful = false AND la.attemptTime > :sinceTime")
    long countFailedLoginAttemptsByUserIdSince(@Param("userId") Long userId, @Param("sinceTime") LocalDateTime sinceTime);

    @Query("SELECT la FROM LoginAttempt la WHERE la.user = :user AND la.successful = false ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentFailedLoginAttempts(@Param("user") User user);

    @Query("SELECT la FROM LoginAttempt la WHERE la.user.id = :userId AND la.successful = false ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentFailedLoginAttemptsByUserId(@Param("userId") Long userId);

    @Query("SELECT la.ipAddress, COUNT(la) as attemptCount FROM LoginAttempt la WHERE la.user = :user AND la.successful = false AND la.attemptTime > :sinceTime GROUP BY la.ipAddress")
    List<Object[]> findFailedLoginAttemptsByIp(@Param("user") User user, @Param("sinceTime") LocalDateTime sinceTime);

    void deleteByUser(User user);

    void deleteByUserId(Long userId);
}
