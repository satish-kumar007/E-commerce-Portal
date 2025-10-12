package com.users.repository;

import com.users.entity.VerificationToken;
import com.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user = :user AND vt.type = :type AND vt.used = false AND vt.expiryDate > :now")
    Optional<VerificationToken> findActiveToken(@Param("user") User user, @Param("type") VerificationToken.Type type, @Param("now") LocalDateTime now);
}
