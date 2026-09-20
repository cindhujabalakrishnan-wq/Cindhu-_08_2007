package com.insurance.platform.dto.policy;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating/updating an insurance policy. Expiry must be after start.
 */
public class PolicyRequest {

    @NotBlank
    private String policyName;

    @NotNull
    private Long insuranceCompanyId;

    @NotNull
    private Long policyTypeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Premium amount must be >= 0")
    private BigDecimal premiumAmount;

    @Size(max = 50)
    private String premiumFrequency;

    @DecimalMin(value = "0.0", inclusive = true, message = "Coverage amount must be >= 0")
    private BigDecimal coverageAmount;

    @Size(max = 255)
    private String nomineeName;

    @Size(max = 100)
    private String nomineeContact;

    private String notes;

    /** Bean-validation helper: expiry must be strictly after start. */
    @jakarta.validation.constraints.AssertTrue(message = "expiryDate must be after startDate")
    public boolean isExpiryAfterStart() {
        if (startDate == null || expiryDate == null) {
            return true;
        }
        return expiryDate.isAfter(startDate);
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Long getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public void setInsuranceCompanyId(Long insuranceCompanyId) {
        this.insuranceCompanyId = insuranceCompanyId;
    }

    public Long getPolicyTypeId() {
        return policyTypeId;
    }

    public void setPolicyTypeId(Long policyTypeId) {
        this.policyTypeId = policyTypeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getPremiumFrequency() {
        return premiumFrequency;
    }

    public void setPremiumFrequency(String premiumFrequency) {
        this.premiumFrequency = premiumFrequency;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    public String getNomineeContact() {
        return nomineeContact;
    }

    public void setNomineeContact(String nomineeContact) {
        this.nomineeContact = nomineeContact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
