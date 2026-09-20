package com.insurance.platform.service;

import com.insurance.platform.dto.admin.AuditLogResponse;
import com.insurance.platform.model.entity.AuditLog;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists audit trail entries in an independent transaction.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** Records an audit entry; never fails the surrounding transaction. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(User user, String action, String entityType, String entityId,
                       String description, String ipAddress) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUserId(user != null ? user.getId() : null);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setDetails(description);
            entry.setIpAddress(ipAddress);
            entry.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist audit log: {}", ex.getMessage());
        }
    }

    /** Paged audit log for admins. */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> list(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(entry -> {
            AuditLogResponse dto = new AuditLogResponse();
            dto.setId(entry.getId());
            dto.setUserId(entry.getUserId());
            dto.setAction(entry.getAction());
            dto.setEntityType(entry.getEntityType());
            dto.setEntityId(entry.getEntityId());
            dto.setDescription(entry.getDetails());
            dto.setIpAddress(entry.getIpAddress());
            dto.setCreatedAt(entry.getCreatedAt());
            return dto;
        });
    }
}
