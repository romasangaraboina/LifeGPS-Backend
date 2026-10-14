package com.lifegps.backend.controller;

import com.lifegps.backend.entity.User;
import com.lifegps.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User saved = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Your LifeGPS account has been created.",
                "user", Map.of("id", saved.getId(), "name", saved.getName(), "email", saved.getEmail(), "phone", saved.getPhone() == null ? "" : saved.getPhone())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        AuthService.AuthResult result = authService.login(request.get("email"), request.get("password"));
        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "token", result.token(),
                "user", Map.of("id", result.id(), "name", result.name(), "email", result.email(), "phone", result.phone() == null ? "" : result.phone())
        ));
    }
}
