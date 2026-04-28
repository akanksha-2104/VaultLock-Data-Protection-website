package com.dataprotection.dataprotection.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String filename,
        String ipAddress,
        LocalDateTime timestamp) {
}
