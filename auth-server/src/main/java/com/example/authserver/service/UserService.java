package com.example.authserver.service;

import com.example.authserver.entity.Users;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.model.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    DefaultResponse checkEmailInUse(String email);
    DefaultResponse checkUsernameInUse(String username);
    void checkUserNotExists(String email);

    Users saveUser(RegisterRequest registerRequest);
    Users getVerifiedUser(String email);
    Users getUnConfirmedUser(String email);
    UserResponse getUserProfile(HttpServletRequest request);

    void validatePassword(String firstPassword, String secondPassword);
    void updatePassword(PasswordResetRequest request, Users user);
    DefaultResponse updateUsername(HttpServletRequest request, String newUsername);

    void markUserEmailAsVerified(Users user);
    DefaultResponse changeEmailToken(HttpServletRequest httpRequest, String newEmail);
    DefaultResponse approveEmail(HttpServletRequest httpRequest, String email, String code);
}
