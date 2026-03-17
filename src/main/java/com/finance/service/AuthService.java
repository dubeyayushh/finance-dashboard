package com.finance.service;

import com.finance.dto.AuthResponse;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;
import com.finance.model.User;
import com.finance.repository.UserRepository;
import com.finance.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public AuthResponse register(RegisterRequest req) {

        // Check if email already exists
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Build user — hash the password before saving, NEVER store raw
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(user);

        // Generate token immediately so user is logged in after registering
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getEmail()))
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest req) {

        // AuthenticationManager validates email + password against DB
        // Throws BadCredentialsException if invalid — caught in GlobalExceptionHandler
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(), req.getPassword()));

        // If we reach here, credentials are valid — load user and return token
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user.getEmail()))
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}