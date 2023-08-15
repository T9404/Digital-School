package com.example.authserver.repository;

import com.example.authserver.entity.PasswordToken;
import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PasswordTokenRepository extends JpaRepository<PasswordToken, Integer> {
    Optional<PasswordToken> findByToken(String token);

    @Query("SELECT prt FROM PasswordToken prt WHERE prt.user = :user AND prt.active = true")
    List<PasswordToken> findAllActiveToken(Users user);
}
