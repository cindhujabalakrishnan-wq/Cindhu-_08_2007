package com.insurance.platform.dto.renewal;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating a policy renewal. All fields are optional: missing values
 * are defaulted by the service (renewal date = today, new expiry = current
 * expiry + 1 year, premium = current policy premium).
 */
public class RenewalRequest {

    private LocalDate renewalDate;

    private LocalDate newExpiryDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Renewal premium must be >= 0")
    private BigDecimal renewalPremium;

    private String notes;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
