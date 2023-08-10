package com.example.authserver.service;

import com.example.authserver.dto.AuthRequest;
import com.example.authserver.dto.AuthResponse;
import com.example.authserver.dto.RegisterResponse;
import com.example.authserver.entity.RefreshToken;
import com.example.authserver.entity.UserCredential;
import com.example.authserver.repository.RefreshTokenRepository;
import com.example.authserver.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private UserCredentialRepository repository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private RefreshTokenRepository tokenRepository;
    private UserCredentialRepository userRepository;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setTokenRepository(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Autowired
    public void setRepository(UserCredentialRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setUserRepository(UserCredentialRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegisterResponse saveUser(UserCredential user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        checkUserNotExists(user.getName());
        repository.save(user);
        return RegisterResponse.builder()
                .message("User created successfully")
                .status("success")
                .build();
    }

    public AuthResponse generateTokens(AuthRequest authRequest) {
        var user = repository.findByName(authRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found"));
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
        String username = token.getUserCredential().getName();
        var accessToken = jwtService.generateAccessToken(username);
        var refreshTokenNew = jwtService.generateRefreshToken(username);

        UserCredential user = userRepository.findByName(username)
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
        if (repository.findByName(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
    }

    private void saveUserToken(UserCredential user, String jwtToken) {
        var token = RefreshToken.builder()
                .userCredential(user)
                .token(jwtToken)
                .build();
        tokenRepository.save(token);
    }
}
