package com.dataprotection.dataprotection.dto.file;

import java.time.LocalDateTime;
import java.util.Set;

public record FileUploadResponse(
        Long id,
        String originalFilename,
        String storedFilename,
        String contentType,
        long size,
        LocalDateTime uploadedAt,
        String tag,
        Set<String> sharedWithEmails,
        String message,
        String downloadUrl,
        String viewUrl,
        String publicDownloadUrl,
        String publicViewUrl) {
}
