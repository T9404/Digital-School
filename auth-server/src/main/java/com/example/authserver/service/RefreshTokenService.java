package com.example.authserver.service;

import com.example.authserver.entity.RefreshToken;
import com.example.authserver.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private  final RefreshTokenRepository tokenRepository;

    public RefreshTokenService(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public RefreshToken findRefreshToken(String refreshToken) {
        return tokenRepository.findRefreshTokenByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    public void save(RefreshToken token) {
        tokenRepository.save(token);
    }

    public void deleteById(int userId) {
        tokenRepository.deleteById(userId);
    }
}
