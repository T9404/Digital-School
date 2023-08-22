package com.example.notification.controller;

import com.example.notification.exception.RequestFailedException;
import com.example.notification.exception.WeatherInfoException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springdoc.core.utils.PropertyResolverUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestControllerAdvice
public class ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);
    private final PropertyResolverUtils propertyResolverUtils;

    public ErrorController(PropertyResolverUtils propertyResolverUtils) {
        this.propertyResolverUtils = propertyResolverUtils;
    }

    @ExceptionHandler(RequestFailedException.class)
    public ResponseEntity<ErrorResponse> handleRequestFailedException(RequestFailedException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WeatherInfoException.class)
    public ResponseEntity<ErrorResponse> handleWeatherInfoException(WeatherInfoException exception) {
        log.error(exception.getMessage(), exception);
        return handleCustomException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(500).body(new ErrorResponse(LocalDateTime.now(), exception.getMessage(), 500));
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
