package com.example.authserver.service.implementation;

import com.example.authserver.entity.RefreshToken;
import com.example.authserver.exception.token.TokenNotFoundException;
import com.example.authserver.repository.RefreshTokenRepository;
import com.example.authserver.service.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private  final RefreshTokenRepository tokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public RefreshToken findRefreshToken(String refreshToken) {
        return tokenRepository.findRefreshTokenByToken(refreshToken).orElseThrow(TokenNotFoundException::new);
    }

    @Override
    public void save(RefreshToken token) {
        tokenRepository.save(token);
    }

    @Override
    public void deleteById(int userId) {
        tokenRepository.deleteById(userId);
    }
}
