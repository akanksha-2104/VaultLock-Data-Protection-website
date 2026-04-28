package com.dataprotection.dataprotection.controller;

import com.dataprotection.dataprotection.dto.audit.AuditLogResponse;
import com.dataprotection.dataprotection.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /api/admin/audit-logs — all users' audit logs
     * Normal users can only see their own. Admins see everything.
     */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        return ResponseEntity.ok(adminService.getAllAuditLogs());
    }

    /**
     * GET /api/admin/stats — system-wide statistics
     * Used by the admin dashboard: total users, files, events, security alerts
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    /**
     * GET /api/admin/security-events — suspicious activity only
     * Returns only LOGIN_FAILED and DECOY_REDIRECT events across all users
     */
    @GetMapping("/security-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getSecurityEvents() {
        return ResponseEntity.ok(adminService.getSecurityEvents());
    }
}
