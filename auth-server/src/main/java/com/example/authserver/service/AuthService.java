package com.example.authserver.service;

import com.example.authserver.model.request.AuthRequest;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.enums.TokenStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private JwtService jwtService;
    private EmailCodeService emailCodeService;
    private PasswordResetTokenService passwordResetTokenService;
    private RefreshTokenService refreshTokenService;
    private UserService userService;

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setEmailVerificationTokenService(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @Autowired
    public void setPasswordResetTokenService(PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRefreshTokenService(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    public Optional<Users> saveUser(RegisterRequest registerRequest) {
        checkUserNotExists(registerRequest.name());
        return Optional.of(userService.saveUser(registerRequest));
    }

    public DefaultResponse confirmRegister(String code, String email) {
        Users user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isEmailVerified()) {
            return new DefaultResponse("User already confirmed", DefaultStatus.ERROR);
        }
        EmailCode emailCode = emailCodeService.findByToken(code)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        confirmEmailToken(emailCode);
        confirmUser(user);
        return new DefaultResponse("User confirmed successfully", DefaultStatus.SUCCESS);
    }

    private void confirmEmailToken(EmailCode code) {
        emailCodeService.verifyExpiration(code);
        code.setTokenStatus(TokenStatus.STATUS_CONFIRMED);
        emailCodeService.save(code);
    }

    private void confirmUser(Users user) {
        user.setEmailVerified(true);
        userService.saveUser(user);
    }

    public DefaultResponse checkEmailInUse(String email) {
        if (userService.isUserExists(email)) {
            return new DefaultResponse("Email already exists", DefaultStatus.ERROR);
        }
        return new DefaultResponse("Email is available", DefaultStatus.SUCCESS);
    }

    public Optional<EmailCode> remakeRegistrationToken(String currentToken) {
        EmailCode emailCode = emailCodeService.findByToken(currentToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (emailCode.getUser().isEmailVerified()) {
            return Optional.empty();
        }
        return Optional.of(emailCodeService.updateExistingTokenWithNameAndExpiry(emailCode));
    }

    public Optional<PasswordResetToken> createForgotPasswordToken(String email) {
        Users user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        return passwordResetTokenService.createToken(user);
    }

    public DefaultResponse resetPassword(PasswordResetRequest request, String tokenId) {
        PasswordResetToken token = passwordResetTokenService.findTokenById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token invalid"));
        if (passwordResetTokenService.isTokenExpired(token)) {
            throw new RuntimeException("Token expired");
        }
        if (!passwordResetTokenService.isTokenActive(token)) {
            throw new RuntimeException("Token already used");
        }
        updateUserPassword(token, request);
        updateResetToken(token);
        return new DefaultResponse("Password reset successfully", DefaultStatus.SUCCESS);
    }


    private void updateUserPassword(PasswordResetToken token, PasswordResetRequest request) {
        Users user = token.getUser();
        if (userService.isEqualPasswords(request.password(), user.getPassword())) {
            throw new RuntimeException("Password already used");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        userService.updatePassword(request.password(), user);
    }

    private void updateResetToken(PasswordResetToken token) {
        passwordResetTokenService.deleteToken(token);
        token.setActive(false);
        passwordResetTokenService.save(token);
    }

    public AuthResponse generateTokens(AuthRequest authRequest) {
        Users user = userService.getUserByName(authRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        refreshTokenService.deleteById(user.getId());
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        if (userService.isEqualPasswords(authRequest.password(), user.getPassword())) {
            return createTokens(user);
        }
        throw new RuntimeException("Invalid password");
    }

    private AuthResponse createTokens(Users user) {
        String accessToken = jwtService.generateAccessToken(user.getName());
        String refreshToken = jwtService.generateRefreshToken(user.getName());
        saveUserToken(user, refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public DefaultResponse checkUsernameInUse(String username) {
        if (userService.getUserByName(username).isPresent()) {
            return new DefaultResponse("Username already exists", DefaultStatus.ERROR);
        }
        return new DefaultResponse("Username is available", DefaultStatus.SUCCESS);
    }

    public AuthResponse updateTokens(String refreshToken) {
        RefreshToken token = refreshTokenService.findRefreshToken(refreshToken);
        String username = token.getUser().getName();
        String accessToken = jwtService.generateAccessToken(username);
        String refreshTokenNew = jwtService.generateRefreshToken(username);

        Users user = userService.getUserByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        deleteOldToken(token);
        saveUserToken(user, refreshTokenNew);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenNew)
                .build();
    }

    private void deleteOldToken(RefreshToken token) {
        refreshTokenService.delete(token);
    }

    private void checkUserNotExists(String username) {
        if (userService.getUserByName(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
    }

    private void saveUserToken(Users user, String jwtToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(jwtToken)
                .build();
        refreshTokenService.save(token);
    }
}
