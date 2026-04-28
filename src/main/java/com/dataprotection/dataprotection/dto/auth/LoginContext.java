package com.dataprotection.dataprotection.dto.auth;

public record LoginContext(
        String ipAddress,
        String deviceInformation) {
}
