package com.insurance.platform.model.enums;

/**
 * Channels through which a premium can be paid.
 */
public enum PaymentMethod {
    /** Credit card payment. */
    CREDIT_CARD,
    /** Debit card payment. */
    DEBIT_CARD,
    /** Bank transfer. */
    BANK_TRANSFER,
    /** Unified Payments Interface. */
    UPI,
    /** Net-banking payment. */
    NET_BANKING,
    /** Cash payment at a branch. */
    CASH,
    /** Cheque payment. */
    CHEQUE,
    /** Mobile wallet payment. */
    WALLET,
    /** Any other payment channel. */
    OTHER
}
