package com.dataprotection.dataprotection.service;

import com.dataprotection.dataprotection.dto.audit.AuditLogResponse;
import com.dataprotection.dataprotection.enums.AuditAction;
import com.dataprotection.dataprotection.repository.AuditLogRepository;
import com.dataprotection.dataprotection.repository.FileDocumentRepository;
import com.dataprotection.dataprotection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final FileDocumentRepository fileDocumentRepository;

    public AdminService(AuditLogRepository auditLogRepository,
                        UserRepository userRepository,
                        FileDocumentRepository fileDocumentRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.fileDocumentRepository = fileDocumentRepository;
    }

    /**
     * All audit log entries across every user, newest first.
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction().name(),
                        log.getFilename(),
                        log.getIpAddress(),
                        log.getTimestamp()))
                .toList();
    }

    /**
     * System-wide statistics for the admin dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        long totalUsers       = userRepository.count();
        long totalFiles       = fileDocumentRepository.count();
        long totalEvents      = auditLogRepository.count();
        long failedLogins     = auditLogRepository.countByAction(AuditAction.LOGIN_FAILED);
        long decoyRedirects   = auditLogRepository.countByAction(AuditAction.DECOY_REDIRECT);
        long activeThreats    = failedLogins + decoyRedirects;

        return Map.of(
                "totalUsers",     totalUsers,
                "totalFiles",     totalFiles,
                "totalEvents",    totalEvents,
                "failedLogins",   failedLogins,
                "decoyRedirects", decoyRedirects,
                "activeThreats",  activeThreats
        );
    }

    /**
     * Only suspicious events — for the security alert panel.
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getSecurityEvents() {
        return auditLogRepository
                .findByActionInOrderByTimestampDesc(
                        List.of(AuditAction.LOGIN_FAILED, AuditAction.DECOY_REDIRECT))
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction().name(),
                        log.getFilename(),
                        log.getIpAddress(),
                        log.getTimestamp()))
                .toList();
    }
}
