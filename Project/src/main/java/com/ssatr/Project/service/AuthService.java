package com.ssatr.Project.service;

import com.ssatr.Project.dto.*;
import com.ssatr.Project.entity.User;
import com.ssatr.Project.repository.UserRepository;
import com.ssatr.Project.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long accessMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.jwt.accessMinutes}") long accessMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessMinutes = accessMinutes;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new RuntimeException("Username already exists");
        }

        User u = new User();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password())); // hash in campul password
        u.setIsOrganizer(req.isOrganizer());
        userRepository.save(u);
    }

    public AuthResponse login(LoginRequest req) {
        var user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateAccessToken(user.getUsername(), accessMinutes);
        return new AuthResponse(token);
    }
}
