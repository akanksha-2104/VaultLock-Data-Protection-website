package com.dataprotection.dataprotection.repository;

import java.util.List;

import com.dataprotection.dataprotection.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dataprotection.dataprotection.entity.AuditLog;
import com.dataprotection.dataprotection.entity.User;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByUserOrderByTimestampDesc(User user);

    List<AuditLog> findAllByOrderByTimestampDesc();

    long countByAction(AuditAction action);

    List<AuditLog> findByActionInOrderByTimestampDesc(List<AuditAction> actions);
}
