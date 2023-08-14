package com.example.authserver.service;

import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.event.OnCreateResetPasswordLinkEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class AuthService {
    private JwtService jwtService;
    private EmailCodeService emailCodeService;
    private PasswordResetTokenService passwordResetTokenService;
    private RefreshTokenService refreshTokenService;
    private UserService userService;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setEmailVerificationTokenService(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
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

    public DefaultResponse saveUser(RegisterRequest registerRequest) {
        checkUserNotExists(registerRequest.name());
        Users user = userService.saveUser(registerRequest);
        UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/email/confirm");
        OnConfirmEmailEvent onConfirmEmailEvent = new OnConfirmEmailEvent(user, urlBuilder, registerRequest.email());
        applicationEventPublisher.publishEvent(onConfirmEmailEvent);
        return new DefaultResponse("User registered successfully. Check your email for confirming", DefaultStatus.SUCCESS);
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

    public DefaultResponse resendToken(String token) {
        EmailCode newToken = remakeRegistrationToken(token);
        UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/email/confirm");
        OnConfirmEmailEvent confirmEmailEvent =
                new OnConfirmEmailEvent(newToken.getUser(), urlBuilder, newToken.getUser().getEmail());
        applicationEventPublisher.publishEvent(confirmEmailEvent);
        return new DefaultResponse("Check your email for confirming", DefaultStatus.SUCCESS);
    }

    private EmailCode remakeRegistrationToken(String currentToken) {
        EmailCode emailCode = emailCodeService.findByToken(currentToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (emailCode.getUser().isEmailVerified()) {
            throw new RuntimeException("Email already confirmed");
        }
        return emailCodeService.updateExistingTokenWithNameAndExpiry(emailCode);
    }

    public DefaultResponse createForgotPasswordToken(String email) {
        Users user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        PasswordResetToken token = passwordResetTokenService.createToken(user)
                .orElseThrow(() -> new RuntimeException("Token not created"));
        UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/password/reset");
        OnCreateResetPasswordLinkEvent onCreateResetPasswordLinkEvent
                = new OnCreateResetPasswordLinkEvent(token, urlBuilder);
        applicationEventPublisher.publishEvent(onCreateResetPasswordLinkEvent);
        return new DefaultResponse("Reset password link sent successfully. Check your email for resetting password", DefaultStatus.SUCCESS);
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
