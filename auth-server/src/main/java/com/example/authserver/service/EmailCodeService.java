package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.TokenStatus;
import com.example.authserver.repository.EmailVerificationTokenRepository;
import com.example.authserver.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class EmailCodeService {
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${app.token.email.verification.duration}")
    private Long emailVerificationTokenExpiryDuration;

    public EmailCodeService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public void createCode(Users user, String token) {
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(token);
        emailCode.setTokenStatus(TokenStatus.STATUS_PENDING);
        emailCode.setUser(user);
        emailCode.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration));
        emailVerificationTokenRepository.save(emailCode);
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
        EmailCode code = emailVerificationTokenRepository.findByCode(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));
        if (code.getUser().isEmailVerified()) {
            throw new RuntimeException("Email already confirmed");
        }
        return code;
    }

    public EmailCode save(EmailCode emailCode) {
        return emailVerificationTokenRepository.save(emailCode);
    }

    public void verifyExpiration(EmailCode token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new RuntimeException("Token has expired");
        }
    }
}
