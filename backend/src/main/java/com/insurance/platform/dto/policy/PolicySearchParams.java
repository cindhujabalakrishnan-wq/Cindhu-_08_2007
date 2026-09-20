package com.insurance.platform.dto.policy;

/**
 * Query parameters for policy search/filtering. Bound from query string in the controller.
 */
public class PolicySearchParams {

    private String status;
    private String category;
    private Integer expiringWithinDays;
    private String search;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getExpiringWithinDays() {
        return expiringWithinDays;
    }

    public void setExpiringWithinDays(Integer expiringWithinDays) {
        this.expiringWithinDays = expiringWithinDays;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
