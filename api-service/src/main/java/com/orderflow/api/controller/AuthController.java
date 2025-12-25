package com.orderflow.api.controller;

import com.orderflow.api.security.JwtService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.username()) && "admin".equals(request.password())) {
            return ResponseEntity.ok(new LoginResponse(jwtService.generateToken("admin", List.of("ADMIN"))));
        }
        if ("user".equals(request.username()) && "user".equals(request.password())) {
            return ResponseEntity.ok(new LoginResponse(jwtService.generateToken("user", List.of("USER"))));
        }
        return ResponseEntity.status(401).body(new LoginResponse("invalid"));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token) {
    }
}
