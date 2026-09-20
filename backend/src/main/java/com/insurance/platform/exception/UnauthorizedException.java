package com.insurance.platform.exception;

/**
 * Thrown when authentication is missing or invalid. Maps to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message human-readable reason
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
