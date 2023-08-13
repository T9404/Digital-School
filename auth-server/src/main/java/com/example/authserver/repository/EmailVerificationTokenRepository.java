package com.example.authserver.repository;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailCode, Integer> {
    Optional<EmailCode> findByCode(String code);
    void deleteByUser(Users user);
}
