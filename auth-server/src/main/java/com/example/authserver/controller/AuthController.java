package com.example.authserver.controller;

import com.example.authserver.model.request.*;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<DefaultResponse> createNewUser(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
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
    public ResponseEntity<DefaultResponse> resendRegistrationToken(@RequestParam("token") @Valid String token) {
        return ResponseEntity.ok(authService.resendToken(token));
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
    public ResponseEntity<DefaultResponse> getForgotPasswordLink(
            @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(authService.createForgotPasswordToken(forgotPasswordRequest.email()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<DefaultResponse> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest,
                                                         @RequestParam("token") String token) {
        return ResponseEntity.ok(authService.resetPassword(passwordResetRequest, token));
    }
}
