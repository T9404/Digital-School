package com.example.authserver.service;

import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.entity.Users;
import com.example.authserver.repository.PasswordResetTokenRepository;
import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.token.password.reset.duration}")
    private Long expiration;

    @Autowired
    public PasswordResetTokenService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public PasswordResetToken getValidToken(PasswordResetRequest request, String tokenId) {
        PasswordResetToken token = tokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        matchEmail(token, request.email());
        verifyExpiration(token);
        return token;
    }

    public PasswordResetToken createToken(Users user) {
        PasswordResetToken token = createTokenWithUser(user);
        return tokenRepository.save(token);
    }

    public void save(PasswordResetToken token) {
        tokenRepository.save(token);
    }

    public PasswordResetToken getValidToken(String tokenId) {
        PasswordResetToken token = tokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));
        if (isTokenExpired(token)) {
            throw new RuntimeException("Token expired");
        }
        if (!isTokenActive(token)) {
            throw new RuntimeException("Token already used");
        }
        return token;
    }

    public void updateResetToken(PasswordResetToken token) {
        deleteToken(token);
        token.setActive(false);
        save(token);
    }

    public boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    public boolean isTokenActive(PasswordResetToken token) {
        return token.getActive();
    }

    private void matchEmail(PasswordResetToken token, String requestEmail) {
        if (token.getUser().getEmail().compareToIgnoreCase(requestEmail) != 0) {
            throw new RuntimeException("Invalid password reset token");
        }
    }

    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }

    private void verifyExpiration(PasswordResetToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new RuntimeException("Password reset token has expired");
        }
        if (!token.getActive()) {
            throw new RuntimeException("Password reset token is not active");
        }
    }

    private PasswordResetToken createTokenWithUser(Users user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(Util.generateRandomUuid());
        token.setExpiryDate(Instant.now().plusMillis(expiration));
        token.setActive(true);
        token.setClaimed(false);
        return token;
    }
}
