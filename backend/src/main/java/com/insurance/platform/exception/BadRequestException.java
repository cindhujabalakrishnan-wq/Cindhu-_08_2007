package com.insurance.platform.exception;

/** Thrown for invalid client input that passes transport validation but fails business rules. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
