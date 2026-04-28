package com.dataprotection.dataprotection.dto.auth;

public record AuthResponse(
        Long userId,
        String name,
        String email,
        String role,
        String message,
        String currentLocation,
        String previousLocation,
        boolean newLocationDetected,
        String token) {
}
