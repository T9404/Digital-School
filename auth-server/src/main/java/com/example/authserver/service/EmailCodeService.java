package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.TokenStatus;
import com.example.authserver.repository.EmailVerificationTokenRepository;
import com.example.authserver.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailCodeService {
    private final EmailVerificationTokenRepository repository;

    @Value("${app.token.email.verification.duration}")
    private Long emailVerificationTokenExpiryDuration;

    public EmailCodeService(EmailVerificationTokenRepository repository) {
        this.repository = repository;
    }

    public void createCode(Users user, String token) {
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(token);
        emailCode.setTokenStatus(TokenStatus.STATUS_PENDING);
        emailCode.setUser(user);
        emailCode.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration));
        repository.save(emailCode);
    }

    public void confirmEmailToken(EmailCode code) {
        verifyExpiration(code);
        code.setTokenStatus(TokenStatus.STATUS_CONFIRMED);
        save(code);
    }

    public EmailCode updateExistingTokenWithName(EmailCode existingToken) {
        existingToken.setTokenStatus(TokenStatus.STATUS_PENDING);
        existingToken.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration));
        return save(existingToken);
    }

    public String generateNewToken() {
        return Util.generateRandomCode();
    }

    public EmailCode getConfirmedToken(String token) {
        EmailCode code = repository.findByCode(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));
        ensureEmailNotVerified(code.getUser());
        return code;
    }

    private void ensureEmailNotVerified(Users user) {
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already confirmed");
        }
    }

    private EmailCode save(EmailCode emailCode) {
        return repository.save(emailCode);
    }

    private void verifyExpiration(EmailCode token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new RuntimeException("Token has expired");
        }
    }
}
