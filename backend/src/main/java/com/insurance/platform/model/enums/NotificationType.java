package com.insurance.platform.model.enums;

/**
 * Channels / purposes of a user notification.
 */
public enum NotificationType {
    /** Policy is approaching expiry. */
    POLICY_EXPIRY,
    /** A premium payment succeeded. */
    PAYMENT_SUCCESS,
    /** A premium payment failed. */
    PAYMENT_FAILED,
    /** A premium is due or overdue. */
    PAYMENT_DUE,
    /** A renewal request changed state. */
    RENEWAL,
    /** A document was uploaded or processed. */
    DOCUMENT,
    /** Generic system announcement. */
    SYSTEM,
    /** Any other notification. */
    OTHER
}
