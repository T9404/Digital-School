package com.example.authserver.service.implementation;

import com.example.authserver.entity.Users;
import com.example.authserver.exception.token.TokenAlreadyUsedException;
import com.example.authserver.exception.token.TokenExpiredException;
import com.example.authserver.exception.token.TokenNotFoundException;
import com.example.authserver.repository.PasswordTokenRepository;
import com.example.authserver.entity.PasswordToken;
import com.example.authserver.service.PasswordTokenService;
import com.example.authserver.util.CodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PasswordTokenServiceImpl implements PasswordTokenService {
    private final PasswordTokenRepository tokenRepository;

    @Value("${app.token.password.reset.duration}")
    private Long expiration;

    public PasswordTokenServiceImpl(PasswordTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public PasswordToken createToken(Users user) {
        PasswordToken token = createTokenWithUser(user);
        return saveToken(token);
    }

    private PasswordToken saveToken(PasswordToken token) {
        return tokenRepository.save(token);
    }

    private PasswordToken createTokenWithUser(Users user) {
        PasswordToken token = new PasswordToken();
        token.setUser(user);
        token.setToken(CodeUtil.generateRandomUuid());
        token.setExpiryDate(Instant.now().plusMillis(expiration));
        token.setActive(true);
        token.setClaimed(false);
        return token;
    }

    @Override
    public PasswordToken getValidToken(String tokenId) {
        PasswordToken token = findTokenById(tokenId);
        validateTokenStatus(token);
        return token;
    }

    private PasswordToken findTokenById(String tokenId) {
        return tokenRepository.findByToken(tokenId).orElseThrow(TokenNotFoundException::new);
    }

    private void validateTokenStatus(PasswordToken token) {
        if (isTokenExpired(token)) {
            throw new TokenExpiredException();
        }
        if (isTokenNotActive(token)) {
            throw new TokenAlreadyUsedException();
        }
    }

    private boolean isTokenExpired(PasswordToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    private boolean isTokenNotActive(PasswordToken token) {
        return token.getActive();
    }

    @Override
    public void updateResetToken(PasswordToken token) {
        deleteToken(token);
        token.setActive(false);
        saveToken(token);
    }

    private void deleteToken(PasswordToken token) {
        tokenRepository.delete(token);
    }
}
