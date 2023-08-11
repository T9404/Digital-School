package com.example.authserver.service;

import com.example.authserver.dto.*;
import com.example.authserver.entity.EmailVerificationToken;
import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.enums.TokenStatus;
import com.example.authserver.repository.RefreshTokenRepository;
import com.example.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private EmailVerificationTokenService emailVerificationTokenService;
    private RefreshTokenRepository tokenRepository;
    private UserRepository userRepository;
    private PasswordResetTokenService passwordResetTokenService;

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
    public void setPasswordResetTokenService(PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @Autowired
    public void setTokenRepository(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Users> saveUser(RegisterRequest registerRequest) {
        checkUserNotExists(registerRequest.name());
        Users user = new Users();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        userRepository.save(user);
        return Optional.of(user);
    }

    public DefaultResponse confirmRegister(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        Users user = emailVerificationToken.getUser();
        if (user.isEmailVerified()) {
            return new DefaultResponse("User already confirmed", DefaultStatus.ERROR);
        }
        confirmEmailToken(emailVerificationToken);
        confirmUser(user);
        return new DefaultResponse("User confirmed successfully", DefaultStatus.SUCCESS);
    }

    private void confirmEmailToken(EmailVerificationToken token) {
        emailVerificationTokenService.verifyExpiration(token);
        token.setTokenStatus(TokenStatus.STATUS_CONFIRMED);
        emailVerificationTokenService.save(token);
    }

    private void confirmUser(Users user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }


    public DefaultResponse checkEmailInUse(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return new DefaultResponse("Email already exists", DefaultStatus.ERROR);
        }
        return new DefaultResponse("Email is available", DefaultStatus.SUCCESS);
    }

    public Optional<EmailVerificationToken> remakeRegistrationToken(String currentToken) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(currentToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (emailVerificationToken.getUser().isEmailVerified()) {
            return Optional.empty();
        }
        return Optional.of(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken));
    }

    public Optional<PasswordResetToken> createForgotPasswordToken(String email) {
        return userRepository.findByEmail(email)
                .map(passwordResetTokenService::createToken)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public DefaultResponse resetPassword(PasswordResetRequest request) {
        PasswordResetToken token = passwordResetTokenService.findTokenById(request.token())
                .orElseThrow(() -> new RuntimeException("Token invalid"));
        if (isTokenExpired(token)) {
            throw new RuntimeException("Token expired");
        }
        updateUserPassword(token, request);
        updateResetToken(token);
        return new DefaultResponse("Password reset successfully", DefaultStatus.SUCCESS);
    }

    private boolean isTokenExpired(PasswordResetToken token) {
        return token.getExpiryDate().isBefore(Instant.from(LocalDateTime.now()));
    }

    private void updateUserPassword(PasswordResetToken token, PasswordResetRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        Users user = token.getUser();
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private void updateResetToken(PasswordResetToken token) {
        token.setActive(false);
        passwordResetTokenService.save(token);
    }

    public Optional<Users> resetPasswordOld(PasswordResetRequest request) {
        PasswordResetToken token = passwordResetTokenService.getValidToken(request);
        String encodedPassword = passwordEncoder.encode(request.password());
        return Optional.of(token)
                .map(passwordResetTokenService::claimToken)
                .map(PasswordResetToken::getUser)
                .map(user -> {
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    return user;
                });
    }

    public AuthResponse generateTokens(AuthRequest authRequest) {
        var user = userRepository.findByName(authRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        if (passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            return createTokens(user);
        }
        throw new RuntimeException("Invalid password");
    }

    private AuthResponse createTokens(Users user) {
        var accessToken = jwtService.generateAccessToken(user.getName());
        var refreshToken = jwtService.generateRefreshToken(user.getName());
        saveUserToken(user, refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public DefaultResponse checkUsernameInUse(String username) {
        if (userRepository.findByName(username).isPresent()) {
            return new DefaultResponse("Username already exists", DefaultStatus.ERROR);
        }
        return new DefaultResponse("Username is available", DefaultStatus.SUCCESS);
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
