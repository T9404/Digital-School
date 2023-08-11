package com.example.authserver.repository;

import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByUser(Users user);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user AND prt.active = true")
    List<PasswordResetToken> findAllActiveToken(Users user);
}
