package com.insurance.platform.dto.document;

/**
 * Heuristic extraction preview for an uploaded policy document.
 */
public class ExtractionPreviewResponse {

    private String policyNumber;
    private String startDate;
    private String expiryDate;
    private String premiumAmount;
    private String coverageAmount;
    private String insurerName;
    private String rawTextExcerpt;

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(String premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(String coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public String getInsurerName() {
        return insurerName;
    }

    public void setInsurerName(String insurerName) {
        this.insurerName = insurerName;
    }

    public String getRawTextExcerpt() {
        return rawTextExcerpt;
    }

    public void setRawTextExcerpt(String rawTextExcerpt) {
        this.rawTextExcerpt = rawTextExcerpt;
    }
}
