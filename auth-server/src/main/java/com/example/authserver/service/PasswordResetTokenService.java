package com.example.authserver.service;

import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.Users;

public interface PasswordResetTokenService {
    PasswordResetToken createToken(Users user);
    PasswordResetToken getValidToken(String tokenId);
    void updateResetToken(PasswordResetToken token);
}
