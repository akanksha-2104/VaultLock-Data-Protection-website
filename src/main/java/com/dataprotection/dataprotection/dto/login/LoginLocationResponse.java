package com.dataprotection.dataprotection.dto.login;

import java.time.LocalDateTime;

public record LoginLocationResponse(
        Long id,
        String ipAddress,
        String location,
        String deviceInformation,
        LocalDateTime loginTime,
        String previousLocation,
        boolean newLocationDetected) {
}
