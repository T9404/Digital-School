package com.example.authserver.service;

import com.example.authserver.model.request.AuthRequest;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;

public interface AuthService {
    DefaultResponse register(RegisterRequest registerRequest);
    DefaultResponse confirmRegister(String code, String email);

    DefaultResponse resendToken(String code);
    AuthResponse generateTokens(AuthRequest authRequest);
    AuthResponse updateTokens(String refreshToken);

    DefaultResponse createForgotPasswordToken(String email);
    DefaultResponse resetPassword(PasswordResetRequest request, String tokenId);
}
