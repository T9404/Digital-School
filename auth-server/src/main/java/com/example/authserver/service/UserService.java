package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.model.request.AuthRequest;
import com.example.authserver.model.request.EmailTokenRequest;
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

    public void saveUser(Users user) {
        userRepository.save(user);
    }

    public Users getVerifiedUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not confirmed");
        }
        return user;
    }

    public Users getVerifiedUser(AuthRequest authRequest) {
        Users user = getVerifiedUser(authRequest.email());
        if (!isEqualPasswords(authRequest.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public Optional<Users> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public boolean isEqualPasswords(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public Users getUnConfirmedUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already confirmed");
        }
        return user;
    }

    public void updatePassword(PasswordResetRequest request, Users user) {
        if (isEqualPasswords(request.password(), user.getPassword())) {
            throw new RuntimeException("Password already used");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public UserResponse getUserProfile(HttpServletRequest request) {
        Users user = getUserFromRequest(request);
        return UserResponse.builder()
                .username(user.getName())
                .email(user.getEmail())
                .isVerified(user.isEmailVerified())
                .build();
    }

    public DefaultResponse changeUsername(HttpServletRequest request, String newUsername) {
        Users user = getUserFromRequest(request);
        user.setName(newUsername);
        userRepository.save(user);
        return new DefaultResponse("Username changed successfully", DefaultStatus.SUCCESS);
    }

    private Users getUserFromRequest(HttpServletRequest request) {
        String token = extractTokenFromHeader(request.getHeader("Authorization"));
        String username = jwtService.extractUsername(token);
        return getUserByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public void confirmUserEmail(Users user) {
        user.setEmailVerified(true);
        saveUser(user);
    }

    public DefaultResponse sendEmailToken(HttpServletRequest httpRequest, String newEmail) {
        Users user = getUserFromRequest(httpRequest);
        if (newEmail.equals(user.getEmail())) {
            throw new RuntimeException("Emails are the same");
        }
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Current email isn't confirmed");
        }
        UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/confirmEmail");
        OnConfirmEmailEvent onConfirmEmailEvent = new OnConfirmEmailEvent(user, urlBuilder, newEmail);
        applicationEventPublisher.publishEvent(onConfirmEmailEvent);
        return new DefaultResponse("Email token sent successfully", DefaultStatus.SUCCESS);
    }

    public DefaultResponse approveEmail(HttpServletRequest httpRequest, String email, String code) {
        Users user = getUserFromRequest(httpRequest);
        EmailCode emailToken = emailTokenRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        if (!emailToken.getUser().equals(user)) {
            throw new RuntimeException("Token doesn't belong to this user");
        }
        user.setEmail(email);
        user.setEmailVerified(true);
        userRepository.save(user);
        return new DefaultResponse("Email confirmed successfully", DefaultStatus.SUCCESS);
    }

    public boolean isEmailInUse(String email) {
        return userRepository.existsByEmail(email);
    }
}
