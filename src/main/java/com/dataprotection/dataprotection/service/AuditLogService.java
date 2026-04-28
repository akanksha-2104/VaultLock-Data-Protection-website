package com.dataprotection.dataprotection.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dataprotection.dataprotection.entity.AuditLog;
import com.dataprotection.dataprotection.entity.User;
import com.dataprotection.dataprotection.dto.audit.AuditLogResponse;
import com.dataprotection.dataprotection.enums.AuditAction;
import com.dataprotection.dataprotection.exception.ResourceNotFoundException;
import com.dataprotection.dataprotection.repository.AuditLogRepository;
import com.dataprotection.dataprotection.repository.UserRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void log(User user, AuditAction action, String filename, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setFilename(filename);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listCurrentUserLogs(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return auditLogRepository.findAllByUserOrderByTimestampDesc(user).stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction().name(),
                        log.getFilename(),
                        log.getIpAddress(),
                        log.getTimestamp()))
                .toList();
    }
}
