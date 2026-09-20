package com.insurance.platform.model.enums;

/**
 * Settlement states of a premium payment.
 */
public enum PaymentStatus {
    /** Payment is scheduled but not yet made. */
    PENDING,
    /** Payment completed successfully. */
    COMPLETED,
    /** Payment failed. */
    FAILED,
    /** Payment was refunded. */
    REFUNDED,
    /** Payment is overdue. */
    OVERDUE,
    /** Payment was cancelled. */
    CANCELLED
}
