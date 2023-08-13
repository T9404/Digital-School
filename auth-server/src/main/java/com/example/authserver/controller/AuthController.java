package com.example.authserver.controller;

import com.example.authserver.model.request.*;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnCreateResetPasswordLinkEvent;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/register")
    public ResponseEntity<DefaultResponse> createNewUser(@RequestBody @Valid RegisterRequest registerRequest) {
        return authService.saveUser(registerRequest)
                .map(user -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/auth/email/confirm");
                    OnConfirmEmailEvent onConfirmEmailEvent
                            = new OnConfirmEmailEvent(user, urlBuilder, registerRequest.email());
                    applicationEventPublisher.publishEvent(onConfirmEmailEvent);
                    return ResponseEntity.ok(new DefaultResponse(
                            "User registered successfully. Check your email for confirming",
                            DefaultStatus.SUCCESS));
                })
                .orElseThrow(() -> new RuntimeException("User already exists"));
    }

    @GetMapping("/checkEmail")
    public ResponseEntity<DefaultResponse> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.checkEmailInUse(email));
    }

    @GetMapping("/checkUsername")
    public ResponseEntity<DefaultResponse> checkUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(authService.checkUsernameInUse(username));
    }

    @GetMapping("register/resendToken")
    public ResponseEntity<DefaultResponse> resendRegistrationToken(@RequestParam("token") @Valid String code) {
        EmailCode newToken = authService.remakeRegistrationToken(code)
                .orElseThrow(() -> new RuntimeException("User is already registered"));
        return Optional.of(newToken.getUser())
                .map(registerUser -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/auth/email/confirm");
                    OnConfirmEmailEvent confirmEmailEvent =
                            new OnConfirmEmailEvent(registerUser, urlBuilder, newToken.getUser().getEmail());
                    applicationEventPublisher.publishEvent(confirmEmailEvent);
                    return ResponseEntity.ok(new DefaultResponse(
                            "Check your email for confirming",
                            DefaultStatus.SUCCESS));
                })
                .orElseThrow(() -> new RuntimeException("User already exists"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthRequest loginRequest) {
        return ResponseEntity.ok(authService.generateTokens(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> updateTokens(@RequestBody @Valid RefreshRequest refresh) {
        return ResponseEntity.ok(authService.updateTokens(refresh.refreshToken()));
    }

    @GetMapping("/email/confirm")
    public ResponseEntity<DefaultResponse> confirmRegister(@RequestParam String token,
                                                           @RequestParam String email) {
        return ResponseEntity.ok(authService.confirmRegister(token, email));
    }

    @PostMapping("/password/forgotLink")
    public ResponseEntity<DefaultResponse> getForgotPasswordLink(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return authService.createForgotPasswordToken(forgotPasswordRequest.email())
                .map(passwordResetToken -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/auth/password/reset");
                    OnCreateResetPasswordLinkEvent onCreateResetPasswordLinkEvent
                            = new OnCreateResetPasswordLinkEvent(passwordResetToken, urlBuilder);
                    applicationEventPublisher.publishEvent(onCreateResetPasswordLinkEvent);
                    return ResponseEntity.ok(new DefaultResponse(
                            "Reset password link sent successfully. Check your email for resetting password",
                            DefaultStatus.SUCCESS));
                })
                .orElseThrow(() -> new RuntimeException("User already exists"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<DefaultResponse> resetPassword(
            @RequestBody @Valid PasswordResetRequest passwordResetRequest,
            @RequestParam("token") String token) {
        return ResponseEntity.ok(authService.resetPassword(passwordResetRequest, token));
    }
}
