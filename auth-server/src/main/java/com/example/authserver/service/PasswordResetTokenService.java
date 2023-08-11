package com.example.authserver.service;

import com.example.authserver.dto.PasswordResetRequest;
import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.Users;
import com.example.authserver.repository.PasswordResetTokenRepository;
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

    public PasswordResetToken getValidToken(PasswordResetRequest request) {
        String tokenId = request.token();
        PasswordResetToken token = tokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        matchEmail(token, request.email());
        verifyExpiration(token);
        return token;
    }

    public Optional<PasswordResetToken> createToken(Users user) {
        PasswordResetToken token = createTokenWithUser(user);
        return Optional.of(tokenRepository.save(token));
    }

    public void save(PasswordResetToken token) {
        tokenRepository.save(token);
    }

    public Optional<PasswordResetToken> findTokenById(String tokenId) {
        return Optional.ofNullable(tokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token")));
    }

    public boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    private void matchEmail(PasswordResetToken token, String requestEmail) {
        if (token.getUser().getEmail().compareToIgnoreCase(requestEmail) != 0) {
            throw new RuntimeException("Invalid password reset token");
        }
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

    public PasswordResetToken claimToken(PasswordResetToken token) {
        Users user = token.getUser();
        token.setClaimed(true);

        /*CollectionUtils.emptyIfNull(repository.findActiveTokensForUser(user))
                .forEach(t -> t.setActive(false));*/
        // rewrite comment without CollectionUtils
        tokenRepository.findAllActiveToken(user).forEach(t -> t.setActive(false));

        return token;
    }
}
