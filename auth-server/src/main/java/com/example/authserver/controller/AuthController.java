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

    @GetMapping("register/resendToken")
    public ResponseEntity<DefaultResponse> resendRegistrationToken(@RequestParam("token") @Valid String token) {
        return ResponseEntity.ok(authService.resendToken(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthRequest loginRequest) {
        return ResponseEntity.ok(authService.generateTokens(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> updateTokens(@RequestBody @Valid RefreshRequest refresh) {
        return ResponseEntity.ok(authService.updateTokens(refresh.refreshToken()));
    }

    @GetMapping("/email/confirm")
    public ResponseEntity<DefaultResponse> confirmRegister(@RequestParam String token,
                                                           @RequestParam String email) {
        return ResponseEntity.ok(authService.confirmRegister(token, email));
    }

    @PostMapping("/password/forgotLink")
    public ResponseEntity<DefaultResponse> getForgotPasswordLink(
            @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(authService.createForgotPasswordToken(forgotPasswordRequest.email()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<DefaultResponse> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest,
                                                         @RequestParam("token") String token) {
        return ResponseEntity.ok(authService.resetPassword(passwordResetRequest, token));
    }
}
