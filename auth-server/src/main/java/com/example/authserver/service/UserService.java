package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.model.request.AuthRequest;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.model.response.UserResponse;
import com.example.authserver.entity.Users;
import com.example.authserver.repository.EmailVerificationTokenRepository;
import com.example.authserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;
    private EmailVerificationTokenRepository emailTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setRefreshTokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setEmailTokenRepository(EmailVerificationTokenRepository emailTokenRepository) {
        this.emailTokenRepository = emailTokenRepository;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public Users saveUser(RegisterRequest registerRequest) {
        Users user = new Users();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        return userRepository.save(user);
    }

    public Users getVerifiedUser(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        checkIfEmailConfirm(user);
        return user;
    }

    private void checkIfEmailConfirm(Users user) {
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
    }

    public Users takeVerifiedUser(AuthRequest authRequest) {
        Users user = getVerifiedUser(authRequest.email());
        validatePassword(authRequest.password(), user.getPassword());
        return user;
    }

    private void validatePassword(String firstPassword, String secondPassword) {
        if (!areEqualPasswords(firstPassword, secondPassword)) {
            throw new RuntimeException("Invalid password");
        }
    }

    public Optional<Users> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public boolean areEqualPasswords(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public Users getUnConfirmedUser(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        ensureEmailNotConfirmed(user);
        return user;
    }

    private void ensureEmailNotConfirmed(Users user) {
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already confirmed");
        }
    }

    public void updatePassword(PasswordResetRequest request, Users user) {
        ensurePasswordNotUsed(request.password(), user.getPassword());
        ensurePasswordsMatch(request.password(), request.confirmPassword());
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private void ensurePasswordNotUsed(String gettingPassword, String userPassword) {
        if (areEqualPasswords(gettingPassword, userPassword)) {
            throw new RuntimeException("Password already used");
        }
    }

    private void ensurePasswordsMatch(String password, String confirmPassword) {
        if (!areEqualPasswords(password, confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }
    }

    public UserResponse getUserProfile(HttpServletRequest request) {
        Users user = fetchUserFromRequest(request);
        return UserResponse.builder()
                .username(user.getName())
                .email(user.getEmail())
                .isVerified(user.isEmailVerified())
                .build();
    }

    public DefaultResponse updateUsername(HttpServletRequest request, String newUsername) {
        Users user = fetchUserFromRequest(request);
        user.setName(newUsername);
        userRepository.save(user);
        return new DefaultResponse("Username changed successfully", DefaultStatus.SUCCESS);
    }

    private Users fetchUserFromRequest(HttpServletRequest request) {
        String token = extractTokenFromAuthorizationHeader(request.getHeader("Authorization"))
                .orElseThrow(() -> new RuntimeException("Token not found"));
        String username = jwtService.extractUsername(token);
        return getUserByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Optional<String> extractTokenFromAuthorizationHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

    public void markUserEmailAsVerified(Users user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public DefaultResponse sendEmailToken(HttpServletRequest httpRequest, String newEmail) {
        Users user = fetchUserFromRequest(httpRequest);
        ensureEmailsAreDifferent(newEmail, user.getEmail());
        ensureEmailConfirmed(user);
        UriComponentsBuilder uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/confirmEmail");
        OnConfirmEmailEvent onConfirmEmailEvent = new OnConfirmEmailEvent(user, uri, newEmail);
        applicationEventPublisher.publishEvent(onConfirmEmailEvent);
        return new DefaultResponse("Email token sent successfully", DefaultStatus.SUCCESS);
    }

    private void ensureEmailsAreDifferent(String newEmail, String oldEmail) {
        if (newEmail.equals(oldEmail)) {
            throw new RuntimeException("Emails are the same");
        }
    }

    private void ensureEmailConfirmed(Users user) {
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
    }

    public DefaultResponse approveEmail(HttpServletRequest httpRequest, String email, String code) {
        Users user = fetchUserFromRequest(httpRequest);
        EmailCode emailToken = emailTokenRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        ensureTokenBelongsToUser(user, emailToken);
        user.setEmail(email);
        markUserEmailAsVerified(user);
        return new DefaultResponse("Email confirmed successfully", DefaultStatus.SUCCESS);
    }

    private void ensureTokenBelongsToUser(Users user, EmailCode emailToken) {
        if (!emailToken.getUser().equals(user)) {
            throw new RuntimeException("Token doesn't belong to this user");
        }
    }

    public boolean isEmailInUse(String email) {
        return userRepository.existsByEmail(email);
    }
}
