package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.platform.model.enums.RenewalStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A renewal request extending cover of an {@link InsurancePolicy}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "policy_renewals", indexes = {
    @Index(name = "idx_renewals_policy", columnList = "policy_id"),
    @Index(name = "idx_renewals_status", columnList = "status")
})
public class PolicyRenewal {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Policy being renewed. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    /** Previous cover end date. */
    @Column(name = "previous_expiry_date")
    private LocalDate previousExpiryDate;

    /** New cover end date after renewal. */
    @Column(name = "new_expiry_date")
    private LocalDate newExpiryDate;

    /** Premium charged for the renewal term. */
    @Column(name = "renewal_premium", precision = 15, scale = 2)
    private BigDecimal renewalPremium;

    /** Current state of the renewal request. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private RenewalStatus status;

    /** When the renewal was requested. */
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /** When the renewal was processed (approved/rejected/completed). */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** Reviewer remarks. */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** Row creation timestamp. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Row last-update timestamp. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Initialises audit timestamps on insert. */
    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (requestedAt == null) {
            requestedAt = now;
        }
    }

    /** Refreshes the update timestamp on modification. */
    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
