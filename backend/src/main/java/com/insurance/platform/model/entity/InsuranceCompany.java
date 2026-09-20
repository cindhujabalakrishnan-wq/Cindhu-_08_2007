package com.insurance.platform.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Insurance carrier / underwriter offering policies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "insurance_companies", indexes = {
    @Index(name = "idx_insurance_companies_code", columnList = "code", unique = true),
    @Index(name = "idx_insurance_companies_name", columnList = "name"),
    @Index(name = "idx_insurance_companies_active", columnList = "active")
})
public class InsuranceCompany {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the carrier. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Short unique carrier code (e.g. LIC, HDFC-ERGO). */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Contact email address. */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    /** Contact phone number. */
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    /** Registered address. */
    @Column(length = 500)
    private String address;

    /** Carrier website URL. */
    @Column(length = 255)
    private String website;

    /** Whether the carrier may be selected for new policies. */
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
