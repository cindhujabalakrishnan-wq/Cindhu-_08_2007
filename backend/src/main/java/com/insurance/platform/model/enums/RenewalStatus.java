package com.insurance.platform.model.enums;

/**
 * Lifecycle states of a policy renewal request.
 */
public enum RenewalStatus {
    /** Renewal requested, awaiting review. */
    PENDING,
    /** Renewal approved and applied. */
    APPROVED,
    /** Renewal rejected. */
    REJECTED,
    /** Renewal completed (policy dates extended). */
    COMPLETED,
    /** Renewal request cancelled. */
    CANCELLED
}
