package com.insurance.platform.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable audit trail entry for security-sensitive operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AuditLog {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Acting user id (nullable for system actions). */
    @Column(name = "user_id")
    private Long userId;

    /** Action performed (e.g. POLICY_CREATED, LOGIN). */
    @Column(nullable = false, length = 100)
    private String action;

    /** Type of entity acted upon (e.g. InsurancePolicy). */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /** Id of the entity acted upon, as string to support any key type. */
    @Column(name = "entity_id", length = 100)
    private String entityId;

    /** Additional structured / free-form details. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Source IP address of the request. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Row creation timestamp. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Initialises the creation timestamp on insert. */
    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
