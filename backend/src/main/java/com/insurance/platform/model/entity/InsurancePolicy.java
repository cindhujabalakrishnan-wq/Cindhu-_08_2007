package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.PremiumFrequency;
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
 * A sold insurance policy owned by a customer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "insurance_policies", indexes = {
    @Index(name = "idx_policies_number", columnList = "policy_number", unique = true),
    @Index(name = "idx_policies_customer", columnList = "customer_id"),
    @Index(name = "idx_policies_company", columnList = "insurance_company_id"),
    @Index(name = "idx_policies_type", columnList = "policy_type_id"),
    @Index(name = "idx_policies_status", columnList = "status"),
    @Index(name = "idx_policies_expiry", columnList = "expiry_date"),
    @Index(name = "idx_policies_customer_status", columnList = "customer_id,status")
})
public class InsurancePolicy {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable unique policy number (e.g. POL-2026-XXXXXX). */
    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;

    /** Owning customer account. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /** Underwriting carrier. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insurance_company_id", nullable = false)
    private InsuranceCompany insuranceCompany;

    /** Sold product. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_type_id", nullable = false)
    private PolicyType policyType;

    /** Free-form display name given at purchase time. */
    @Column(name = "policy_name", nullable = false, length = 200)
    private String policyName;

    /** Business line of this policy. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PolicyCategory category;

    /** Cover start date (inclusive). */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Cover end date (inclusive). */
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    /** Premium payable per frequency cycle. */
    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    /** How often the premium is payable. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "premium_frequency", length = 30)
    private PremiumFrequency premiumFrequency;

    /** Sum insured. */
    @Column(name = "coverage_amount", precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    /** Current lifecycle state. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PolicyStatus status;

    /** Nominee full name. */
    @Column(name = "nominee_name", length = 255)
    private String nomineeName;

    /** Nominee contact details. */
    @Column(name = "nominee_contact", length = 100)
    private String nomineeContact;

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
