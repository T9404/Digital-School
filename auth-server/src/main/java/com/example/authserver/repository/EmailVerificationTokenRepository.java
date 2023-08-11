package com.example.authserver.repository;

import com.example.authserver.entity.EmailVerificationToken;
import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser(Users user);
}
