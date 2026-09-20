package com.insurance.platform.exception;

/** Thrown when the current principal is not allowed to access a resource. Maps to HTTP 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
