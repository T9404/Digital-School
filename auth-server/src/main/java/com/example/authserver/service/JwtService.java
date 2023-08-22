package com.example.authserver.service;

public interface JwtService {
    String generateAccessToken(String userName);

    String generateRefreshToken(String userName);

    String extractUsername(String token);
}
