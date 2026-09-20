package com.insurance.platform.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Extended KYC / demographic profile for a customer {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_profiles", indexes = {
    @Index(name = "idx_customer_profiles_user", columnList = "user_id", unique = true)
})
public class CustomerProfile {

    /** Surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning user; one profile per user. */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Date of birth of the customer. */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Gender label supplied by the customer. */
    @Column(length = 30)
    private String gender;

    /** Street address. */
    @Column(length = 255)
    private String address;

    /** City of residence. */
    @Column(length = 100)
    private String city;

    /** State / province of residence. */
    @Column(length = 100)
    private String state;

    /** Postal / ZIP code. */
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    /** Country of residence. */
    @Column(length = 100)
    private String country;

    /** Occupation of the customer. */
    @Column(length = 100)
    private String occupation;

    /** Declared annual income. */
    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;

    /** Type of identity proof (e.g. PASSPORT, AADHAAR). */
    @Column(name = "id_proof_type", length = 50)
    private String idProofType;

    /** Identity proof document number. */
    @Column(name = "id_proof_number", length = 100)
    private String idProofNumber;

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
