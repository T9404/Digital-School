package com.example.authserver.service;

import io.jsonwebtoken.Claims;
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
import java.util.function.Function;

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
        return generateToken(userName, accessExpirationTime, accessSecretKey);
    }

    public String generateRefreshToken(String userName) {
        return generateToken(userName, refreshExpirationTime, refreshSecretKey);
    }

    private String generateToken(String userName, int expirationTime, String secretKey) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(secretKey), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey(accessSecretKey)).build().parseClaimsJws(token).getBody();
    }
}
