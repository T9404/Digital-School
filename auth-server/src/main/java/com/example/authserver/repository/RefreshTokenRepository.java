package com.example.authserver.repository;

import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findRefreshTokenByToken(String token);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_token WHERE user_id = ?1", nativeQuery = true)
    void deleteById(int userId);
}
