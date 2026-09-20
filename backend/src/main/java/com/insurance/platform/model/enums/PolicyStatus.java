package com.insurance.platform.model.enums;

/**
 * Lifecycle states of an insurance policy.
 */
public enum PolicyStatus {
    /** Policy is active and in force. */
    ACTIVE,
    /** Policy expires within 30 days and needs attention. */
    EXPIRING_SOON,
    /** Policy has expired and is no longer in force. */
    EXPIRED,
    /** Renewal has been requested and is awaiting processing. */
    PENDING_RENEWAL,
    /** Policy was renewed; historic record kept for reference. */
    RENEWED,
    /** Policy was cancelled before its natural expiry. */
    CANCELLED,
    /** Policy is suspended (e.g. missed premium). */
    SUSPENDED
}
