package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.platform.model.enums.PaymentMethod;
import com.insurance.platform.model.enums.PaymentStatus;
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
 * A single premium payment against an {@link InsurancePolicy}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "premium_payments", indexes = {
    @Index(name = "idx_payments_policy", columnList = "policy_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_due_date", columnList = "due_date"),
    @Index(name = "idx_payments_txn_ref", columnList = "transaction_reference")
})
public class PremiumPayment {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Policy this payment belongs to. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    /** Amount paid / due. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Date the payment was made (null while pending). */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /** Date the payment is / was due. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Channel used for the payment. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    /** Gateway / bank transaction reference. */
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    /** Settlement state. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    /** Free-form notes. */
    @Column(columnDefinition = "TEXT")
    private String notes;

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
    }

    /** Refreshes the update timestamp on modification. */
    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
