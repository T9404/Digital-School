package com.example.authserver.repository;

import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findRefreshTokenByToken(String token);
    void deleteByUser(Users user);
}
