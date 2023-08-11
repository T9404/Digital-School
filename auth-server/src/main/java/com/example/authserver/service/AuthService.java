package com.example.authserver.service;

import com.example.authserver.dto.*;
import com.example.authserver.entity.EmailVerificationToken;
import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.repository.RefreshTokenRepository;
import com.example.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private MailService senderService;
    private EmailVerificationTokenService emailVerificationTokenService;
    private RefreshTokenRepository tokenRepository;
    private UserRepository userRepository;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setEmailVerificationTokenService(EmailVerificationTokenService emailVerificationTokenService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @Autowired
    public void setTokenRepository(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setSenderService(MailService senderService) {
        this.senderService = senderService;
    }

    public Optional<Users> saveUser(RegisterRequest registerRequest) {
        checkUserNotExists(registerRequest.name());
        Users user = new Users(registerRequest.name(), registerRequest.email(), passwordEncoder.encode(registerRequest.password()));
        userRepository.save(user);
        // senderService.sendSimpleEmail("ghubman1@gmail.com", "Subject", "Body");
        return Optional.of(user);
    }

    public DefaultResponse confirmRegister(String token) {
        var emailVerificationToken = emailVerificationTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        var user = emailVerificationToken.getUser();
        if (user.isEmailVerified()) {
            return new DefaultResponse("User already confirmed", DefaultStatus.ERROR);
        }

        emailVerificationTokenService.verifyExpiration(emailVerificationToken);
        emailVerificationToken.setConfirmed();
        emailVerificationTokenService.save(emailVerificationToken);

        user.setEmailVerified(true);
        userRepository.save(user);
        return new DefaultResponse("User confirmed successfully", DefaultStatus.SUCCESS);
    }

    public Optional<EmailVerificationToken> remakeRegistrationToken(String currentToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(currentToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (emailVerificationToken.getUser().isEmailVerified()) {
            return Optional.empty();
        }
        return Optional.of(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken));
    }

    public AuthResponse generateTokens(AuthRequest authRequest) {
        var user = userRepository.findByName(authRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("User not confirmed");
        }
        if (passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            var accessToken = jwtService.generateAccessToken(user.getName());
            var refreshToken = jwtService.generateRefreshToken(user.getName());
            saveUserToken(user, refreshToken);
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        throw new RuntimeException("Invalid password");
    }

    public AuthResponse updateTokens(String refreshToken) {
        var token = tokenRepository.findRefreshTokenByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        String username = token.getUser().getName();
        var accessToken = jwtService.generateAccessToken(username);
        var refreshTokenNew = jwtService.generateRefreshToken(username);

        Users user = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        deleteOldToken(token);
        saveUserToken(user, refreshTokenNew);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenNew)
                .build();
    }

    private void deleteOldToken(RefreshToken token) {
        tokenRepository.delete(token);
    }

    private void checkUserNotExists(String username) {
        if (userRepository.findByName(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
    }

    private void saveUserToken(Users user, String jwtToken) {
        var token = RefreshToken.builder()
                .user(user)
                .token(jwtToken)
                .build();
        tokenRepository.save(token);
    }
}
