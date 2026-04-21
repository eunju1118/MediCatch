package com.medicatch.health.controller;

import com.medicatch.health.dto.request.LoginRequest;
import com.medicatch.health.dto.request.RegisterRequest;
import com.medicatch.health.dto.response.ApiResponse;
import com.medicatch.health.dto.response.AuthResponse;
import com.medicatch.health.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // /api/auth/register → StripPrefix=2 → /register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null));
    }

    // /api/auth/login → StripPrefix=2 → /login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
