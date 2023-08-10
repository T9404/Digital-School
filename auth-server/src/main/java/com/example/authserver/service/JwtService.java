package com.example.authserver.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {

    @Value("${jwt.access.secret.key}")
    private String accessSecretKey;

    @Value("${jwt.access.expiration.time}")
    private int accessExpirationTime;

    @Value("${jwt.refresh.secret.key}")
    private String refreshSecretKey;

    @Value("${jwt.refresh.expiration.time}")
    private int refreshExpirationTime;

    public String generateAccessToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createAccessToken(claims, userName);
    }

    public String generateRefreshToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createRefreshToken(claims, userName);
    }

    private String createRefreshToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(getRefreshSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private String createAccessToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationTime))
                .signWith(getAccessSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getAccessSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getRefreshSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
