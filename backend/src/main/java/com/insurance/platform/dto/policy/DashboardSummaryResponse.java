package com.insurance.platform.dto.policy;

import java.math.BigDecimal;

/**
 * Per-customer dashboard summary shown on the policy overview screen.
 */
public class DashboardSummaryResponse {

    private long totalPolicies;
    private long activePolicies;
    private long expiredPolicies;
    private long expiringSoon;
    private BigDecimal totalPremiumPaid;
    private BigDecimal totalPendingAmount;

    public long getTotalPolicies() {
        return totalPolicies;
    }

    public void setTotalPolicies(long totalPolicies) {
        this.totalPolicies = totalPolicies;
    }

    public long getActivePolicies() {
        return activePolicies;
    }

    public void setActivePolicies(long activePolicies) {
        this.activePolicies = activePolicies;
    }

    public long getExpiredPolicies() {
        return expiredPolicies;
    }

    public void setExpiredPolicies(long expiredPolicies) {
        this.expiredPolicies = expiredPolicies;
    }

    public long getExpiringSoon() {
        return expiringSoon;
    }

    public void setExpiringSoon(long expiringSoon) {
        this.expiringSoon = expiringSoon;
    }

    public BigDecimal getTotalPremiumPaid() {
        return totalPremiumPaid;
    }

    public void setTotalPremiumPaid(BigDecimal totalPremiumPaid) {
        this.totalPremiumPaid = totalPremiumPaid;
    }

    public BigDecimal getTotalPendingAmount() {
        return totalPendingAmount;
    }

    public void setTotalPendingAmount(BigDecimal totalPendingAmount) {
        this.totalPendingAmount = totalPendingAmount;
    }
}
