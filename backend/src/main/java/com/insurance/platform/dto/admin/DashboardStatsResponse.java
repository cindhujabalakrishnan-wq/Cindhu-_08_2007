package com.insurance.platform.dto.admin;

import com.insurance.platform.dto.policy.PolicyResponse;
import com.insurance.platform.dto.renewal.RenewalResponse;
import com.insurance.platform.dto.user.UserResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Platform-wide statistics for the admin dashboard.
 */
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalPolicies;
    private long activePolicies;
    private long expiringSoon;
    private long expiredPolicies;
    private BigDecimal totalPremium;
    private long pendingRenewals;
    private long overduePayments;
    private long unreadNotifications;
    private List<PolicyResponse> recentPolicies;
    private List<UserResponse> recentUsers;
    private List<RenewalResponse> upcomingRenewals;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

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

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public long getPendingRenewals() {
        return pendingRenewals;
    }

    public void setPendingRenewals(long pendingRenewals) {
        this.pendingRenewals = pendingRenewals;
    }

    public long getOverduePayments() {
        return overduePayments;
    }

    public void setOverduePayments(long overduePayments) {
        this.overduePayments = overduePayments;
    }

    public long getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(long unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    public List<PolicyResponse> getRecentPolicies() {
        return recentPolicies;
    }

    public void setRecentPolicies(List<PolicyResponse> recentPolicies) {
        this.recentPolicies = recentPolicies;
    }

    public List<UserResponse> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<UserResponse> recentUsers) {
        this.recentUsers = recentUsers;
    }

    public List<RenewalResponse> getUpcomingRenewals() {
        return upcomingRenewals;
    }

    public void setUpcomingRenewals(List<RenewalResponse> upcomingRenewals) {
        this.upcomingRenewals = upcomingRenewals;
    }
}
