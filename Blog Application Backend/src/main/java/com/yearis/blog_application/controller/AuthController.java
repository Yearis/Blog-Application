package com.yearis.blog_application.controller;

import com.yearis.blog_application.payload.request.LoginRequest;
import com.yearis.blog_application.payload.request.RegisterRequest;
import com.yearis.blog_application.payload.response.JwtAuthResponse;
import com.yearis.blog_application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Rest API Endpoints", description = "Operations related to authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /// --- Authentication Operations ---

    ///  Registration

    // register a user
    @Operation(summary = "Register a user", description = "Create new user")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {

        String response = authService.register(registerRequest);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /// Login

    // login an existing user
    @Operation(summary = "Login a user", description = "Submit email & password to authenticate user")
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        JwtAuthResponse response = authService.login(loginRequest);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
