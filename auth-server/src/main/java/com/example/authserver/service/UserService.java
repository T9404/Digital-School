package com.example.authserver.service;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.model.request.EmailTokenRequest;
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

    public boolean isUserExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<Users> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public boolean isEqualPasswords(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updatePassword(String updatedPassword, Users user) {
        String encodedPassword = passwordEncoder.encode(updatedPassword);
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
}
