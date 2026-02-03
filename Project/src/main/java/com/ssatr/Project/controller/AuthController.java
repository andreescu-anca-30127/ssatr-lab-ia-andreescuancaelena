package com.ssatr.Project.controller;

import com.ssatr.Project.dto.*;
import com.ssatr.Project.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }


    @GetMapping("/ping")
    public String ping() {
        return "AUTH_OK";
    }

    @PostMapping("/register")
    public ResponseEntity<SubmitSurveyResponse> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(new SubmitSurveyResponse("Registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
