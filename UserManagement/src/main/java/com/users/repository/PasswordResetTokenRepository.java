package com.users.repository;

import com.users.entity.PasswordResetToken;
import com.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user AND prt.used = false AND prt.expiryDate > :now")
    Optional<PasswordResetToken> findActiveTokenForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    void deleteByUser(User user);
}
