package com.example.authserver.controller;

import com.example.authserver.dto.*;
import com.example.authserver.entity.EmailVerificationToken;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnRegenerateEmailVerificationEvent;
import com.example.authserver.event.OnUserRegistrationCompleteEvent;
import com.example.authserver.service.AuthService;
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
    public ResponseEntity<DefaultResponse> createNewUser(@RequestBody RegisterRequest registerRequest) {
        return authService.saveUser(registerRequest)
                .map(user -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/auth/register/confirm");
                    OnUserRegistrationCompleteEvent onUserRegistrationCompleteEvent
                            = new OnUserRegistrationCompleteEvent(user, urlBuilder);
                    applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent);
                    return ResponseEntity.ok(new DefaultResponse(
                            "User registered successfully. Check your email for confirming",
                            DefaultStatus.SUCCESS));
                })
                .orElseThrow(() -> new RuntimeException("User already exists"));
    }

    @GetMapping("register/resendToken")
    public ResponseEntity<DefaultResponse> resendRegistrationToken(@RequestParam("token") EmailTokenRequest tokenRequest) {
        EmailVerificationToken newToken = authService.remakeRegistrationToken(tokenRequest.token())
                .orElseThrow(() -> new RuntimeException("User is already registered"));
        return Optional.of(newToken.getUser())
                .map(registerUser -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/auth/register/confirm");
                    OnRegenerateEmailVerificationEvent regenerateEmailVerificationEvent =
                            new OnRegenerateEmailVerificationEvent(registerUser, urlBuilder, newToken);
                    applicationEventPublisher.publishEvent(regenerateEmailVerificationEvent);
                    return ResponseEntity.ok(new DefaultResponse(
                            "User registered successfully. Check your email for confirming",
                            DefaultStatus.SUCCESS));
                })
                .orElseThrow(() -> new RuntimeException("User already exists"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> getToken(@RequestBody AuthRequest loginRequest) {
        return ResponseEntity.ok(authService.generateTokens(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> updateTokens(@RequestBody RefreshRequest refresh) {
        return ResponseEntity.ok(authService.updateTokens(refresh.refreshToken()));
    }

    @GetMapping("/register/confirm")
    public ResponseEntity<DefaultResponse> confirmRegister(@RequestParam("token") EmailTokenRequest tokenRequest) {
        return ResponseEntity.ok(authService.confirmRegister(tokenRequest.token()));
    }
}
