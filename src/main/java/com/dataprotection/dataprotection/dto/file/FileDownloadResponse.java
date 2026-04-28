package com.dataprotection.dataprotection.dto.file;

import org.springframework.core.io.Resource;

public record FileDownloadResponse(
        Resource resource,
        String originalFilename,
        String contentType) {
}
