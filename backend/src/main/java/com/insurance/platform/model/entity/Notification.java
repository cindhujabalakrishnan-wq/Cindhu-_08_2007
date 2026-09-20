package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.platform.model.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user-facing notification (expiry reminders, payment events, ...).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user", columnList = "user_id"),
    @Index(name = "idx_notifications_user_read", columnList = "user_id,is_read"),
    @Index(name = "idx_notifications_type", columnList = "type")
})
public class Notification {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Recipient of the notification. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Purpose / channel of the notification. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** Short headline. */
    @Column(nullable = false, length = 255)
    private String title;

    /** Full body text. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Optionally linked policy id (denormalised, no FK to allow policy deletion). */
    @Column(name = "related_policy_id")
    private Long relatedPolicyId;

    /** Whether the notification has been read. */
    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** When the notification was read (null while unread). */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** Row creation timestamp. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Initialises defaults and timestamps on insert. */
    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
