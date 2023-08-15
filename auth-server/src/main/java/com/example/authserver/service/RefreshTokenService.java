package com.example.authserver.service;

import com.example.authserver.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken findRefreshToken(String refreshToken);
    void save(RefreshToken token);
    void deleteById(int userId);
}
