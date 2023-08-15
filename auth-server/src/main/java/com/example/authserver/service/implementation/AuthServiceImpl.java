package com.example.authserver.service.implementation;

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
import com.example.authserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthServiceImpl implements AuthService {
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

    @Override
    public DefaultResponse register(RegisterRequest registerRequest) {
        userService.checkUserNotExists(registerRequest.email());
        Users user = userService.saveUser(registerRequest);
        UriComponentsBuilder uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/email/confirm");
        OnConfirmEmailEvent event = new OnConfirmEmailEvent(user, uri, registerRequest.email());
        applicationEventPublisher.publishEvent(event);
        return new DefaultResponse("User registered successfully. Check your email for confirming", DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse confirmRegister(String code, String email) {
        Users user = userService.getUnConfirmedUser(email);
        EmailCode emailCode = emailCodeService.getConfirmedToken(code);
        emailCodeService.confirmEmailToken(emailCode);
        userService.markUserEmailAsVerified(user);
        return new DefaultResponse("User confirmed successfully", DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse resendToken(String code) {
        EmailCode newToken = remakeRegistrationToken(code);
        UriComponentsBuilder uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/email/confirm");
        OnConfirmEmailEvent event = new OnConfirmEmailEvent(newToken.getUser(), uri, newToken.getUser().getEmail());
        applicationEventPublisher.publishEvent(event);
        return new DefaultResponse("Check your email for confirming", DefaultStatus.SUCCESS);
    }

    private EmailCode remakeRegistrationToken(String currentToken) {
        EmailCode emailCode = emailCodeService.getConfirmedToken(currentToken);
        return emailCodeService.updateExistingTokenWithName(emailCode);
    }

    @Override
    public DefaultResponse createForgotPasswordToken(String email) {
        Users user = userService.getVerifiedUser(email);
        PasswordResetToken token = passwordResetTokenService.createToken(user);
        UriComponentsBuilder uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/password/reset");
        OnCreateResetPasswordLinkEvent event = new OnCreateResetPasswordLinkEvent(token, uri);
        applicationEventPublisher.publishEvent(event);
        return new DefaultResponse("Reset password link sent successfully. Check your email for resetting password", DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse resetPassword(PasswordResetRequest request, String tokenId) {
        PasswordResetToken token = passwordResetTokenService.getValidToken(tokenId);
        userService.updatePassword(request, token.getUser());
        passwordResetTokenService.updateResetToken(token);
        return new DefaultResponse("Password reset successfully", DefaultStatus.SUCCESS);
    }

    @Override
    public AuthResponse generateTokens(AuthRequest authRequest) {
        Users user = userService.getVerifiedUser(authRequest.email());
        userService.validatePassword(authRequest.password(), user.getPassword());
        refreshTokenService.deleteById(user.getId());
        return createTokens(user);
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

    private void saveUserToken(Users user, String jwtToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(jwtToken)
                .build();
        refreshTokenService.save(token);
    }

    @Override
    public AuthResponse updateTokens(String refreshToken) {
        RefreshToken token = refreshTokenService.findRefreshToken(refreshToken);
        String email = token.getUser().getEmail();
        Users user = userService.getVerifiedUser(email);
        refreshTokenService.deleteById(user.getId());
        return createTokens(user);
    }
}
