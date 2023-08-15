package com.example.authserver.service.implementation;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.exception.email.EmailAlreadyConfirmedException;
import com.example.authserver.exception.email.EmailNotConfirmedException;
import com.example.authserver.exception.email.EmailsSameException;
import com.example.authserver.exception.password.InvalidPasswordException;
import com.example.authserver.exception.password.PasswordNotMatchException;
import com.example.authserver.exception.password.PasswordUsedException;
import com.example.authserver.exception.token.TokenNotFoundException;
import com.example.authserver.exception.user.UserAlreadyExistsException;
import com.example.authserver.exception.user.UserNotFoundException;
import com.example.authserver.model.request.PasswordResetRequest;
import com.example.authserver.model.request.RegisterRequest;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.model.response.UserResponse;
import com.example.authserver.entity.Users;
import com.example.authserver.repository.UserRepository;
import com.example.authserver.service.EmailCodeService;
import com.example.authserver.service.JwtService;
import com.example.authserver.service.UserService;
import com.example.authserver.util.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private EmailCodeService emailCodeService;
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
    public void setEmailTokenRepository(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Users saveUser(RegisterRequest registerRequest) {
        Users user = new Users();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        return userRepository.save(user);
    }

    @Override
    public void checkUserNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }
    }

    @Override
    public Users getVerifiedUser(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        checkIfEmailConfirm(user);
        return user;
    }

    private void checkIfEmailConfirm(Users user) {
        if (!user.isEmailVerified()) {
            throw new EmailNotConfirmedException();
        }
    }

    @Override
    public void validatePassword(String firstPassword, String secondPassword) {
        if (!areEqualPasswords(firstPassword, secondPassword)) {
            throw new InvalidPasswordException();
        }
    }

    @Override
    public DefaultResponse checkUsernameInUse(String username) {
        if (getUserByName(username).isPresent()) {
            return new DefaultResponse(MessageUtil
                    .getMessage("api.user.is-available.api-response.400.description"), DefaultStatus.ERROR);
        }
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.is-available.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse checkEmailInUse(String email) {
        if (isEmailInUse(email)) {
            return new DefaultResponse(MessageUtil
                    .getMessage("api.email.is-available.api-response.400.description"), DefaultStatus.ERROR);
        }
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.is-available.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private Optional<Users> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    private boolean areEqualPasswords(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Override
    public Users getUnConfirmedUser(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        ensureEmailNotConfirmed(user);
        return user;
    }

    private void ensureEmailNotConfirmed(Users user) {
        if (user.isEmailVerified()) {
            throw new EmailAlreadyConfirmedException();
        }
    }

    @Override
    public void updatePassword(PasswordResetRequest request, Users user) {
        ensurePasswordNotUsed(request.password(), user.getPassword());
        ensurePasswordsMatch(request.password(), request.confirmPassword());
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private void ensurePasswordNotUsed(String gettingPassword, String userPassword) {
        if (areEqualPasswords(gettingPassword, userPassword)) {
            throw new PasswordUsedException();
        }
    }

    private void ensurePasswordsMatch(String password, String confirmPassword) {
        if (!areEqualPasswords(password, confirmPassword)) {
            throw new PasswordNotMatchException();
        }
    }

    @Override
    public UserResponse getUserProfile(HttpServletRequest request) {
        Users user = fetchUserFromRequest(request);
        return UserResponse.builder()
                .username(user.getName())
                .email(user.getEmail())
                .isVerified(user.isEmailVerified())
                .build();
    }

    @Override
    public DefaultResponse updateUsername(HttpServletRequest request, String newUsername) {
        Users user = fetchUserFromRequest(request);
        user.setName(newUsername);
        userRepository.save(user);
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.change.name.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private Users fetchUserFromRequest(HttpServletRequest request) {
        String token = extractTokenFromAuthorizationHeader(request.getHeader("Authorization"))
                .orElseThrow(TokenNotFoundException::new);
        String username = jwtService.extractUsername(token);
        return getUserByName(username).orElseThrow(UserNotFoundException::new);
    }

    private Optional<String> extractTokenFromAuthorizationHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

    @Override
    public void markUserEmailAsVerified(Users user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public DefaultResponse changeEmailToken(HttpServletRequest httpRequest, String newEmail) {
        Users user = fetchUserFromRequest(httpRequest);
        ensureEmailsAreDifferent(newEmail, user.getEmail());
        ensureEmailConfirmed(user);
        UriComponentsBuilder uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(MessageUtil.getMessage("api.user.path.confirm-email"));
        OnConfirmEmailEvent onConfirmEmailEvent = new OnConfirmEmailEvent(user, uri, newEmail);
        applicationEventPublisher.publishEvent(onConfirmEmailEvent);
        return new DefaultResponse(MessageUtil
                .getMessage("api.token.sent.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private void ensureEmailsAreDifferent(String newEmail, String oldEmail) {
        if (newEmail.equals(oldEmail)) {
            throw new EmailsSameException();
        }
    }

    private void ensureEmailConfirmed(Users user) {
        if (!user.isEmailVerified()) {
            throw new EmailNotConfirmedException();
        }
    }

    @Override
    public DefaultResponse approveEmail(HttpServletRequest httpRequest, String email, String code) {
        Users user = fetchUserFromRequest(httpRequest);
        EmailCode emailToken = emailCodeService.findByCode(code);
        ensureTokenBelongsToUser(user, emailToken);
        user.setEmail(email);
        markUserEmailAsVerified(user);
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.confirm.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private void ensureTokenBelongsToUser(Users user, EmailCode emailToken) {
        if (!emailToken.getUser().equals(user)) {
            throw new TokenNotFoundException();
        }
    }

    private boolean isEmailInUse(String email) {
        return userRepository.existsByEmail(email);
    }
}
