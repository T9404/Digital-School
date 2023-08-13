package com.example.authserver.controller;

import com.example.authserver.model.response.DefaultResponse;
import com.example.authserver.model.response.UserResponse;
import com.example.authserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUserProfile(request));
    }

    @PostMapping("/changeUsername")
    public ResponseEntity<DefaultResponse> changeUsername(HttpServletRequest request,
                                                          @RequestParam("newUsername") String newUsername) {
        return ResponseEntity.ok(userService.changeUsername(request, newUsername));
    }

    @PostMapping("/changeEmail")
    public ResponseEntity<DefaultResponse> sendEmailToken(HttpServletRequest httpRequest,
                                                          @RequestParam("newEmail") String email) {
        return ResponseEntity.ok(userService.sendEmailToken(httpRequest, email));
    }

    @PostMapping("/confirmEmail")
    public ResponseEntity<DefaultResponse> confirmEmail(HttpServletRequest httpRequest,
                                                        @RequestParam("token") String token,
                                                        @RequestParam("email") String email) {
        return ResponseEntity.ok(userService.approveEmail(httpRequest, email, token));
    }
}