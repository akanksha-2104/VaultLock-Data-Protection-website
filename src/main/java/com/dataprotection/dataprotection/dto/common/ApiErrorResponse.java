package com.dataprotection.dataprotection.dto.common;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details,
        Integer remainingAttempts,
        boolean redirectToFakeUi) {
}
