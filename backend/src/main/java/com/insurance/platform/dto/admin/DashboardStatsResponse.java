package com.insurance.platform.dto.admin;

/**
 * Platform-wide statistics for the admin dashboard.
 */
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalPolicies;
    private long activePolicies;
    private long expiredPolicies;
    private long pendingRenewals;
    private long overduePayments;
    private long unreadNotifications;

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
}
