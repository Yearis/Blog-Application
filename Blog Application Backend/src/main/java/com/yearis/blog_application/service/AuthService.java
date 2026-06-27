package com.yearis.blog_application.service;

import com.yearis.blog_application.payload.request.LoginRequest;
import com.yearis.blog_application.payload.request.RegisterRequest;
import com.yearis.blog_application.payload.response.JwtAuthResponse;

public interface AuthService {
    
    // registration
    String register(RegisterRequest request) throws Exception;

    // login
    JwtAuthResponse login(LoginRequest request);
}
