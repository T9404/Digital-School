package com.example.authserver.service.implementation;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.TokenStatus;
import com.example.authserver.exception.email.EmailAlreadyConfirmedException;
import com.example.authserver.exception.token.TokenExpiredException;
import com.example.authserver.exception.token.TokenNotFoundException;
import com.example.authserver.repository.EmailVerificationTokenRepository;
import com.example.authserver.service.EmailCodeService;
import com.example.authserver.util.CodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {
    private final EmailVerificationTokenRepository repository;

    @Value("${app.token.email.verification.duration}")
    private Long emailVerificationTokenExpiryDuration;

    public EmailCodeServiceImpl(EmailVerificationTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createCode(Users user, String code) {
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setTokenStatus(TokenStatus.STATUS_PENDING);
        emailCode.setUser(user);
        emailCode.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration));
        repository.save(emailCode);
    }

    @Override
    public EmailCode findByCode(String code) {
        return repository.findByCode(code).orElseThrow(TokenNotFoundException::new);
    }

    @Override
    public void confirmEmailToken(EmailCode code) {
        verifyExpiration(code);
        code.setTokenStatus(TokenStatus.STATUS_CONFIRMED);
        save(code);
    }

    @Override
    public EmailCode updateExistingTokenWithName(EmailCode existingToken) {
        existingToken.setTokenStatus(TokenStatus.STATUS_PENDING);
        existingToken.setExpiryDate(Instant.now().plusMillis(emailVerificationTokenExpiryDuration));
        return save(existingToken);
    }

    @Override
    public String generateNewToken() {
        return CodeUtil.generateRandomCode();
    }

    @Override
    public EmailCode getConfirmedToken(String codeId) {
        EmailCode code = repository.findByCode(codeId).orElseThrow(TokenNotFoundException::new);
        ensureEmailNotVerified(code.getUser());
        return code;
    }

    private void ensureEmailNotVerified(Users user) {
        if (user.isEmailVerified()) {
            throw new EmailAlreadyConfirmedException();
        }
    }

    private EmailCode save(EmailCode emailCode) {
        return repository.save(emailCode);
    }

    private void verifyExpiration(EmailCode token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new TokenExpiredException();
        }
    }
}
