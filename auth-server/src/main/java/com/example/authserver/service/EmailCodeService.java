package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.entity.Users;

public interface EmailCodeService {
    void createCode(Users user, String code);
    String generateNewToken();
    EmailCode updateExistingTokenWithName(EmailCode existingToken);

    EmailCode getConfirmedToken(String token);
    void confirmEmailToken(EmailCode code);
    EmailCode findByCode(String code);
}
