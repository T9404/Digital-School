package com.example.authserver.service.implementation;

import com.example.authserver.entity.EmailCode;
import com.example.authserver.enums.DefaultStatus;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.exception.email.EmailAlreadyVerifiedException;
import com.example.authserver.exception.email.EmailNotVerifiedException;
import com.example.authserver.exception.email.DuplicateEmailException;
import com.example.authserver.exception.password.InvalidPasswordFormatException;
import com.example.authserver.exception.password.PasswordMismatchException;
import com.example.authserver.exception.password.PasswordAlreadyUsedException;
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
import org.apache.commons.lang3.StringUtils;

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
        Users user = createUserFromRequest(registerRequest);
        return saveUser(user);
    }

    private Users createUserFromRequest(RegisterRequest registerRequest) {
        Users user = new Users();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        return user;
    }

    private Users saveUser(Users user) {
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
        Users user = findUserByEmail(email);
        ensureEmailConfirmed(user);
        return user;
    }

    private Users findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public void validatePassword(String providedPassword, String storedPassword) {
        if (!areEqualEncodedPasswords(providedPassword, storedPassword)) {
            throw new InvalidPasswordFormatException();
        }
    }

    @Override
    public DefaultResponse checkUsernameInUse(String username) {
        if (getUserByName(username).isPresent()) {
            return createUserNotAvailableResponse();
        }
        return createUserAvailableResponse();
    }

    private DefaultResponse createUserAvailableResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.is-available.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private DefaultResponse createUserNotAvailableResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.is-available.api-response.400.description"), DefaultStatus.ERROR);
    }

    @Override
    public DefaultResponse checkEmailInUse(String email) {
        if (isEmailInUse(email)) {
            return createEmailNotAvailableResponse();
        }
        return createEmailAvailableResponse();
    }

    private boolean isEmailInUse(String email) {
        return userRepository.existsByEmail(email);
    }

    private DefaultResponse createEmailNotAvailableResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.is-available.api-response.400.description"), DefaultStatus.ERROR);
    }

    private DefaultResponse createEmailAvailableResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.is-available.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    private Optional<Users> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    private boolean areEqualEncodedPasswords(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Override
    public Users getUnConfirmedUser(String email) {
        Users user = findUserByEmail(email);
        ensureEmailNotConfirmed(user);
        return user;
    }

    private void ensureEmailNotConfirmed(Users user) {
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }
    }

    @Override
    public void updatePassword(PasswordResetRequest request, Users user) {
        ensurePasswordNotUsed(request.password(), user.getPassword());
        ensurePasswordsMatch(request.password(), request.confirmPassword());
        String encodedPassword = passwordEncoder.encode(request.password());
        updateUserPassword(user, encodedPassword);
    }

    private void ensurePasswordNotUsed(String gettingPassword, String userPassword) {
        if (areEqualEncodedPasswords(gettingPassword, userPassword)) {
            throw new PasswordAlreadyUsedException();
        }
    }

    private void ensurePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }
    }

    private void updateUserPassword(Users user, String encodedPassword) {
        user.setPassword(encodedPassword);
        saveUser(user);
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
        saveUser(user);
        return createUsernameChangedResponse();
    }

    private Users fetchUserFromRequest(HttpServletRequest request) {
        String token = extractAuthorizationToken(request);
        String username = extractUsernameFromToken(token);
        return getUserByUsername(username);
    }

    private String extractAuthorizationToken(HttpServletRequest request) {
        return extractTokenFromAuthorizationHeader(request.getHeader("Authorization"))
                .orElseThrow(TokenNotFoundException::new);
    }

    private Optional<String> extractTokenFromAuthorizationHeader(String header) {
        if (StringUtils.isNotEmpty(header) && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

    private String extractUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    private Users getUserByUsername(String username) {
        return getUserByName(username).orElseThrow(UserNotFoundException::new);
    }

    private DefaultResponse createUsernameChangedResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.user.change.name.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public void markUserEmailAsVerified(Users user) {
        user.setEmailVerified(true);
        saveUser(user);
    }

    @Override
    public DefaultResponse changeEmailToken(HttpServletRequest httpRequest, String newEmail) {
        Users user = fetchUserFromRequest(httpRequest);
        ensureEmailsAreDifferent(newEmail, user.getEmail());
        ensureEmailConfirmed(user);
        sendChangeEmailToken(user, newEmail);
        return createEmailChangedResponse();
    }

    private void ensureEmailsAreDifferent(String newEmail, String oldEmail) {
        if (newEmail.equals(oldEmail)) {
            throw new DuplicateEmailException();
        }
    }

    private void ensureEmailConfirmed(Users user) {
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
    }

    private void sendChangeEmailToken(Users user, String newEmail) {
        UriComponentsBuilder confirmationUri = buildChangeEmailConfirmationUri();
        OnConfirmEmailEvent onConfirmEmailEvent = new OnConfirmEmailEvent(user, confirmationUri, newEmail);
        applicationEventPublisher.publishEvent(onConfirmEmailEvent);
    }

    private UriComponentsBuilder buildChangeEmailConfirmationUri() {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(MessageUtil.getMessage("api.user.path.confirm-email"));
    }

    private DefaultResponse createEmailChangedResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.token.sent.api-response.200.description"), DefaultStatus.SUCCESS);
    }

    @Override
    public DefaultResponse approveEmail(HttpServletRequest httpRequest, String email, String code) {
        Users user = fetchUserFromRequest(httpRequest);
        EmailCode emailToken = getEmailToken(code);
        ensureTokenBelongsToUser(user, emailToken);
        user.setEmail(email);
        markUserEmailAsVerified(user);
        return createEmailVerifiedResponse();
    }

    private EmailCode getEmailToken(String code) {
        return emailCodeService.findByCode(code);
    }

    private void ensureTokenBelongsToUser(Users user, EmailCode emailToken) {
        if (!emailToken.getUser().equals(user)) {
            throw new TokenNotFoundException();
        }
    }

    private DefaultResponse createEmailVerifiedResponse() {
        return new DefaultResponse(MessageUtil
                .getMessage("api.email.confirm.api-response.200.description"), DefaultStatus.SUCCESS);
    }
}
