package com.yearis.blog_application.service.impl;

import com.yearis.blog_application.entity.Role;
import com.yearis.blog_application.entity.User;
import com.yearis.blog_application.payload.request.LoginRequest;
import com.yearis.blog_application.payload.request.RegisterRequest;
import com.yearis.blog_application.payload.response.JwtAuthResponse;
import com.yearis.blog_application.repository.RoleRepository;
import com.yearis.blog_application.repository.UserRepository;
import com.yearis.blog_application.security.JwtService;
import com.yearis.blog_application.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, RoleRepository roleRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }

    private User buildUser(RegisterRequest request) {

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        user.setRoles(Set.of(userRole));

        return user;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) throws Exception {

        // first we check if the username or password is already used or not
        if (userRepository.existsByEmail(request.getEmail()) ||
                userRepository.existsByUsername(request.getUsername())) {

            throw new Exception("Username or email already taken");
        }

        User user = buildUser(request);

        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest request) {

        // we authenticate our user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        String token = jwtService.generateToken(new HashMap<>(), (UserDetails) authentication.getPrincipal());

        return new JwtAuthResponse(token);
    }
}
