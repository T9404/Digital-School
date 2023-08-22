package com.example.authserver.controller;

import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.model.response.UserResponse;
import com.example.authserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "api.user.tag.name", description = "api.user.tag.description")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "api.user.me.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.user.me.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUserProfile(request));
    }

    @Operation(summary = "api.user.change-name.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.user.change-name.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/changeUsername")
    public ResponseEntity<DefaultResponse> changeUsername(HttpServletRequest request,
                                                          @RequestParam("newUsername") String newUsername) {
        return ResponseEntity.ok(userService.updateUsername(request, newUsername));
    }

    @Operation(summary = "api.user.change-email.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.user.change-email.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/changeEmail")
    public ResponseEntity<DefaultResponse> changeEmailToken(HttpServletRequest httpRequest,
                                                            @RequestParam("newEmail") String email) {
        return ResponseEntity.ok(userService.changeEmailToken(httpRequest, email));
    }

    @Operation(summary = "api.user.confirm-email.summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "api.user.confirm-email.200.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DefaultResponse.class))}),
            @ApiResponse(responseCode = "500", description = "api.auth.500.description",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorControllerAdvice.class))})
    })
    @PostMapping("/confirmEmail")
    public ResponseEntity<DefaultResponse> confirmEmail(HttpServletRequest httpRequest,
                                                        @RequestParam("token") String token,
                                                        @RequestParam("email") String email) {
        return ResponseEntity.ok(userService.approveEmail(httpRequest, email, token));
    }
}
