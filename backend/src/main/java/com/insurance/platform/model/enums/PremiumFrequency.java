package com.insurance.platform.model.enums;

/**
 * How often a premium is payable.
 */
public enum PremiumFrequency {
    /** Single one-time premium. */
    ONE_TIME,
    /** Premium payable every month. */
    MONTHLY,
    /** Premium payable every quarter. */
    QUARTERLY,
    /** Premium payable every six months. */
    HALF_YEARLY,
    /** Premium payable once a year. */
    YEARLY
}
