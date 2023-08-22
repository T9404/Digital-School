package com.example.authserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.authserver.model.request.*;
import com.example.authserver.model.response.AuthResponse;
import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.service.AuthService;
import com.example.authserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "api.auth.tag.name", description = "api.auth.tag.description")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "api.auth.register.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.register.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "409", description = "api.auth.register.409.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/register")
    public ResponseEntity<DefaultResponse> createNewUser(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }


    @Operation(summary = "api.auth.checkEmail.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.checkEmail.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "409", description = "api.auth.checkEmail.409.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @GetMapping("/checkEmail")
    public ResponseEntity<DefaultResponse> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.checkEmailInUse(email));
    }

    @Operation(summary = "api.auth.checkUsername.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.checkUsername.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "409", description = "api.auth.checkUsername.409.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @GetMapping("/checkUsername")
    public ResponseEntity<DefaultResponse> checkUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(userService.checkUsernameInUse(username));
    }

    @Operation(summary = "api.auth.resendToken.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.resendToken.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @GetMapping("register/resendToken")
    public ResponseEntity<DefaultResponse> resendRegistrationToken(@RequestParam("token") @Valid String token) {
        return ResponseEntity.ok(authService.resendToken(token));
    }

    @Operation(summary = "api.auth.login.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.login.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))}),
            @ApiResponse(responseCode = "404", description = "api.auth.login.404.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "409", description = "api.auth.login.409.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthRequest loginRequest) {
        return ResponseEntity.ok(authService.generateTokens(loginRequest));
    }

    @Operation(summary = "api.auth.refresh.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.refresh.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))}),
            @ApiResponse(responseCode = "404", description = "api.auth.refresh.404.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> updateTokens(@RequestBody @Valid RefreshRequest refresh) {
        return ResponseEntity.ok(authService.updateTokens(refresh.refreshToken()));
    }

    @Operation(summary = "api.auth.confirm-email.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.confirm-email.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "404", description = "api.auth.confirm-email.404.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "409", description = "api.auth.confirm-email.409.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "410", description = "api.auth.confirm-email.410.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @GetMapping("/email/confirm")
    public ResponseEntity<DefaultResponse> confirmRegister(@RequestParam String token,
                                                           @RequestParam String email) {
        return ResponseEntity.ok(authService.confirmRegister(token, email));
    }

    @Operation(summary = "api.auth.password.link.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.password.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "404", description = "api.auth.password.404.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/password/forgotLink")
    public ResponseEntity<DefaultResponse> getForgotPasswordLink(
            @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(authService.createForgotPasswordToken(forgotPasswordRequest.email()));
    }

    @Operation(summary = "api.auth.password.reset.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.auth.password.reset.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "404", description = "api.auth.password.reset.404.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.password.reset.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/password/reset")
    public ResponseEntity<DefaultResponse> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest,
                                                         @RequestParam("token") String token) {
        return ResponseEntity.ok(authService.resetPassword(passwordResetRequest, token));
    }
}
