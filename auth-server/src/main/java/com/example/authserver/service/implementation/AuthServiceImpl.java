package com.example.authserver.service.implementation;

import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.event.OnCreatePwdResetLinkEvent;
import com.example.authserver.model.request.AuthRequest;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.PasswordToken;
import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.Users;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.service.*;
import com.example.authserver.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthServiceImpl implements AuthService {
    private JwtService jwtService;
    private EmailCodeService emailCodeService;
    private PasswordTokenService passwordTokenService;
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
    public void setPasswordResetTokenService(PasswordTokenService passwordTokenService) {
        this.passwordTokenService = passwordTokenService;
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
        checkIfUserExists(registerRequest.email());
        Users user = saveUser(registerRequest);
        UriComponentsBuilder uri = buildConfirmationUri();
        sendConfirmationEmail(user, uri);
        return createUserCreatedSuccessResponse();
    }

    private void checkIfUserExists(String email) {
        userService.checkUserNotExists(email);
    }

    private Users saveUser(RegisterRequest registerRequest) {
        return userService.saveUser(registerRequest);
    }

    private UriComponentsBuilder buildConfirmationUri() {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(MessageUtil.getMessage("api.auth.path.confirm-email"));
    }

    private void sendConfirmationEmail(Users user, UriComponentsBuilder confirmationUri) {
        OnConfirmEmailEvent event = new OnConfirmEmailEvent(user, confirmationUri, user.getEmail());
        applicationEventPublisher.publishEvent(event);
    }

    private DefaultResponse createUserCreatedSuccessResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.register.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse confirmRegister(String code, String email) {
        Users user = getUserToConfirm(email);
        EmailCode emailCode = getConfirmedEmailCode(code);
        confirmEmailCode(emailCode);
        markUserEmailAsVerified(user);
        return createEmailConfirmedSuccessResponse();
    }

    private Users getUserToConfirm(String email) {
        return userService.getUnConfirmedUser(email);
    }

    private EmailCode getConfirmedEmailCode(String code) {
        return emailCodeService.getConfirmedToken(code);
    }

    private void confirmEmailCode(EmailCode emailCode) {
        emailCodeService.confirmEmailToken(emailCode);
    }

    private void markUserEmailAsVerified(Users user) {
        userService.markUserEmailAsVerified(user);
    }

    private DefaultResponse createEmailConfirmedSuccessResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.confirm.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse resendToken(String code) {
        EmailCode newToken = remakeRegistrationToken(code);
        UriComponentsBuilder uri = buildConfirmationUri();
        sendConfirmationEmail(newToken.getUser(), uri);
        return createTokenSendSuccessResponse();
    }

    private EmailCode remakeRegistrationToken(String currentToken) {
        EmailCode emailCode = emailCodeService.getConfirmedToken(currentToken);
        return emailCodeService.updateExistingTokenWithName(emailCode);
    }

    private DefaultResponse createTokenSendSuccessResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.token.sent.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse createForgotPasswordToken(String email) {
        Users user = getVerifiedUserByEmail(email);
        PasswordToken token = createPasswordResetToken(user);
        UriComponentsBuilder uri = buildResetPasswordUri();
        sendPasswordResetEvent(token, uri);
        return createPasswordSentSuccessResponse();
    }

    private Users getVerifiedUserByEmail(String email) {
        return userService.getVerifiedUser(email);
    }

    private PasswordToken createPasswordResetToken(Users user) {
        return passwordTokenService.createToken(user);
    }

    private UriComponentsBuilder buildResetPasswordUri() {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(MessageUtil.getMessage("api.auth.password.reset"));
    }

    private void sendPasswordResetEvent(PasswordToken token, UriComponentsBuilder resetPasswordUri) {
        OnCreatePwdResetLinkEvent event = new OnCreatePwdResetLinkEvent(token, resetPasswordUri);
        applicationEventPublisher.publishEvent(event);
    }

    private DefaultResponse createPasswordSentSuccessResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.password.sent.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse resetPassword(PasswordResetRequest request, String tokenId) {
        PasswordToken token = getPasswordResetToken(tokenId);
        updateUserPassword(request, token);
        updatePasswordResetToken(token);
        return createPasswordResetSuccessResponse();
    }

    private PasswordToken getPasswordResetToken(String tokenId) {
        return passwordTokenService.getValidToken(tokenId);
    }

    private void updateUserPassword(PasswordResetRequest request, PasswordToken token) {
        Users user = token.getUser();
        userService.updatePassword(request, user);
    }

    private void updatePasswordResetToken(PasswordToken token) {
        passwordTokenService.updateResetToken(token);
    }

    private DefaultResponse createPasswordResetSuccessResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.password.reset.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public AuthResponse generateTokens(AuthRequest authRequest) {
        Users user = getVerifiedUser(authRequest.email());
        validatePassword(authRequest.password(), user.getPassword());
        deleteRefreshToken(user.getId());
        return createTokens(user);
    }

    private Users getVerifiedUser(String email) {
        return userService.getVerifiedUser(email);
    }

    private void validatePassword(String providedPassword, String storedPassword) {
        userService.validatePassword(providedPassword, storedPassword);
    }

    private void deleteRefreshToken(int userId) {
        refreshTokenService.deleteById(userId);
    }

    private AuthResponse createTokens(Users user) {
        String accessToken = generateAccessToken(user.getName());
        String refreshToken = generateRefreshToken(user.getName());
        saveUserToken(user, refreshToken);
        return createTokensResponse(accessToken, refreshToken);
    }

    private String generateAccessToken(String username) {
        return jwtService.generateAccessToken(username);
    }

    private String generateRefreshToken(String username) {
        return jwtService.generateRefreshToken(username);
    }

    private void saveUserToken(Users user, String jwtToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(jwtToken)
                .build();
        refreshTokenService.save(token);
    }

    private AuthResponse createTokensResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse updateTokens(String refreshToken) {
        RefreshToken token = findRefreshToken(refreshToken);
        Users user = getUserByRefreshToken(token);
        deleteRefreshToken(user.getId());
        return createTokens(user);
    }

    private RefreshToken findRefreshToken(String refreshToken) {
        return refreshTokenService.findRefreshToken(refreshToken);
    }

    private Users getUserByRefreshToken(RefreshToken refreshToken) {
        String email = refreshToken.getUser().getEmail();
        return userService.getVerifiedUser(email);
    }
}
