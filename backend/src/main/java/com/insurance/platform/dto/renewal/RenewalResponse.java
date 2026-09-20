package com.insurance.platform.dto.renewal;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Policy renewal view.
 */
public class RenewalResponse {

    private Long id;
    private Long policyId;
    private String policyNumber;
    private LocalDate previousExpiryDate;
    private LocalDate renewalDate;
    private LocalDate newExpiryDate;
    private BigDecimal renewalPremium;
    private String status;
    private String notes;
    private Long daysRemaining;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public LocalDate getPreviousExpiryDate() {
        return previousExpiryDate;
    }

    public void setPreviousExpiryDate(LocalDate previousExpiryDate) {
        this.previousExpiryDate = previousExpiryDate;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
    }

    public LocalDate getNewExpiryDate() {
        return newExpiryDate;
    }

    public void setNewExpiryDate(LocalDate newExpiryDate) {
        this.newExpiryDate = newExpiryDate;
    }

    public BigDecimal getRenewalPremium() {
        return renewalPremium;
    }

    public void setRenewalPremium(BigDecimal renewalPremium) {
        this.renewalPremium = renewalPremium;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
}
