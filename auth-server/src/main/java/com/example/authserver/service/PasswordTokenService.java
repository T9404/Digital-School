package com.example.authserver.service;

import com.example.authserver.entity.PasswordToken;
import com.example.authserver.entity.Users;

public interface PasswordTokenService {
    PasswordToken createToken(Users user);

    PasswordToken getValidToken(String tokenId);

    void updateResetToken(PasswordToken token);
}
