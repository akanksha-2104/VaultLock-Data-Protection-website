package com.dataprotection.dataprotection.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataprotection.dataprotection.dto.auth.AuthResponse;
import com.dataprotection.dataprotection.dto.auth.LoginContext;
import com.dataprotection.dataprotection.dto.auth.LoginRequest;
import com.dataprotection.dataprotection.dto.auth.RegisterRequest;
import com.dataprotection.dataprotection.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request, extractClientIp(httpRequest)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginContext loginContext = new LoginContext(
                extractClientIp(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT));
        return ResponseEntity.ok(authService.login(request, loginContext));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> currentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
    }

    private String extractClientIp(HttpServletRequest httpRequest) {
        String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
