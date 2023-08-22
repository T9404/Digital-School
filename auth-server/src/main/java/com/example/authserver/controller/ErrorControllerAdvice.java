package com.example.authserver.controller;

import com.example.authserver.exception.email.CustomEmailException;
import com.example.authserver.exception.email.EmailAlreadyVerifiedException;
import com.example.authserver.exception.email.EmailNotVerifiedException;
import com.example.authserver.exception.email.DuplicateEmailException;
import com.example.authserver.exception.password.InvalidPasswordFormatException;
import com.example.authserver.exception.password.PasswordMismatchException;
import com.example.authserver.exception.password.PasswordAlreadyUsedException;
import com.example.authserver.exception.token.TokenAlreadyUsedException;
import com.example.authserver.exception.token.TokenExpiredException;
import com.example.authserver.exception.token.TokenNotFoundException;
import com.example.authserver.exception.user.UserAlreadyExistsException;
import com.example.authserver.exception.user.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestControllerAdvice
public class ErrorControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAdvice.class);
    private final PropertyResolverUtils propertyResolverUtils;

    public ErrorControllerAdvice(PropertyResolverUtils propertyResolverUtils) {
        this.propertyResolverUtils = propertyResolverUtils;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(500).body(new ErrorResponse(LocalDateTime.now(), exception.getMessage(), 500));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UserAlreadyExistsException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSignerAlreadyExistsException(UserNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSignerNotFoundException(TokenNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomEmailException.class)
    public ResponseEntity<ErrorResponse> handleCustomMailException(CustomEmailException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleAlbumAlreadyExistsException(TokenExpiredException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleAlbumNotFoundException(TokenAlreadyUsedException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PasswordAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleSongNotFoundException(PasswordAlreadyUsedException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        return handleBindValidationException(exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(PasswordMismatchException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordFormatException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(InvalidPasswordFormatException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailsSameException(DuplicateEmailException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotConfirmedException(EmailNotVerifiedException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyConfirmedException(EmailAlreadyVerifiedException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.CONFLICT);
    }

    public record ErrorResponse(LocalDateTime timestamp, String message, int code) {
    }

    protected ResponseEntity<ErrorResponse> handleCustomException(Exception exception, HttpStatus status) {
        return ResponseEntity.status(status).body(body(exception.getMessage(), status.value()));
    }

    protected ResponseEntity<ErrorResponse> handleBindValidationException(BindException exception) {
        String message = IntStream.range(0, exception.getErrorCount()).mapToObj(i -> i + 1 + "." +
                exception.getAllErrors().get(i).getDefaultMessage()).collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message, 400));
    }

    protected ErrorResponse body(String message, Integer code) {
        return new ErrorResponse(LocalDateTime.now(), message(message), code);
    }

    private String message(String property) {
        return this.propertyResolverUtils.resolve(property, Locale.getDefault());
    }
}
