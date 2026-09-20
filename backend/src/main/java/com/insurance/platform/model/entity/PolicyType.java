package com.insurance.platform.model.entity;

import com.insurance.platform.model.enums.PolicyCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalogue entry describing a sellable policy product.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "policy_types", indexes = {
    @Index(name = "idx_policy_types_code", columnList = "code", unique = true),
    @Index(name = "idx_policy_types_category", columnList = "category"),
    @Index(name = "idx_policy_types_active", columnList = "active")
})
public class PolicyType {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the product. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Short unique product code. */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Business line of the product. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PolicyCategory category;

    /** Marketing / underwriting description. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Minimum permissible sum insured. */
    @Column(name = "min_coverage", precision = 15, scale = 2)
    private BigDecimal minCoverage;

    /** Maximum permissible sum insured. */
    @Column(name = "max_coverage", precision = 15, scale = 2)
    private BigDecimal maxCoverage;

    /** Indicative base premium used for quotations. */
    @Column(name = "base_premium", precision = 15, scale = 2)
    private BigDecimal basePremium;

    /** Whether the product may be selected for new policies. */
    @Column(nullable = false)
    private boolean active;

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
