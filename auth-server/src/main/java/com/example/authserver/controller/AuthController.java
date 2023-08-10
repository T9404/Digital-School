package com.example.authserver.controller;

import com.example.authserver.dto.AuthRequest;
import com.example.authserver.dto.AuthResponse;
import com.example.authserver.dto.RefreshRequest;
import com.example.authserver.dto.RegisterResponse;
import com.example.authserver.entity.UserCredential;
import com.example.authserver.service.AuthService;
import com.example.authserver.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    private final EmailService senderService;

    @Autowired
    public AuthController(AuthService service, EmailService senderService) {
        this.service = service;
        this.senderService = senderService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createNewUser(@RequestBody UserCredential user) {
        senderService.sendSimpleEmail("ghubman1@gmail.com", "Subject", "Body");
        return ResponseEntity.ok(service.saveUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> getToken(@RequestBody AuthRequest loginRequest) {
        return ResponseEntity.ok(service.generateTokens(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> updateTokens(@RequestBody RefreshRequest refresh) {
        return ResponseEntity.ok(service.updateTokens(refresh.refreshToken()));
    }
}
