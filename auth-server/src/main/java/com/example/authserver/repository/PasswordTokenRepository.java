package com.example.authserver.repository;

import com.example.authserver.entity.PasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordTokenRepository extends JpaRepository<PasswordToken, Integer> {
    Optional<PasswordToken> findByToken(String token);
}
