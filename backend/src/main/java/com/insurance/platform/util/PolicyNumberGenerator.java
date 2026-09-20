package com.insurance.platform.util;

import java.security.SecureRandom;
import java.time.LocalDate;

/**
 * Generates unique, human-readable policy numbers of the form {@code POL-YYYY-XXXXXX}.
 */
public final class PolicyNumberGenerator {

    private static final String PREFIX = "POL";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_PART_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PolicyNumberGenerator() {
    }

    /**
     * Generates a new policy number using the current year.
     *
     * @return unique-looking policy number, e.g. {@code POL-2026-A1B2C3}
     */
    public static String generate() {
        return generate(LocalDate.now().getYear());
    }

    /**
     * Generates a new policy number for the given year.
     *
     * @param year four-digit year embedded in the number
     * @return unique-looking policy number, e.g. {@code POL-2026-A1B2C3}
     */
    public static String generate(int year) {
        StringBuilder suffix = new StringBuilder(RANDOM_PART_LENGTH);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            suffix.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return PREFIX + "-" + year + "-" + suffix;
    }
}
