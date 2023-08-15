package com.example.authserver.service.implementation;

import com.example.authserver.entity.Users;
import com.example.authserver.repository.PasswordResetTokenRepository;
import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.service.PasswordResetTokenService;
import com.example.authserver.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.token.password.reset.duration}")
    private Long expiration;

    public PasswordResetTokenServiceImpl(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public PasswordResetToken createToken(Users user) {
        PasswordResetToken token = createTokenWithUser(user);
        return saveToken(token);
    }

    private PasswordResetToken saveToken(PasswordResetToken token) {
        return tokenRepository.save(token);
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

    @Override
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

    private boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    private boolean isTokenActive(PasswordResetToken token) {
        return token.getActive();
    }

    @Override
    public void updateResetToken(PasswordResetToken token) {
        deleteToken(token);
        token.setActive(false);
        saveToken(token);
    }

    private void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }
}
