package com.dataprotection.dataprotection.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dataprotection.dataprotection.dto.login.LoginLocationResponse;
import com.dataprotection.dataprotection.service.LoginLocationService;

@RestController
@RequestMapping("/api/login-locations")
public class LoginLocationController {

    private final LoginLocationService loginLocationService;

    public LoginLocationController(LoginLocationService loginLocationService) {
        this.loginLocationService = loginLocationService;
    }

    @GetMapping
    public ResponseEntity<List<LoginLocationResponse>> listCurrentUserLocations(Authentication authentication) {
        return ResponseEntity.ok(loginLocationService.listCurrentUserLocations(authentication.getName()));
    }
}
