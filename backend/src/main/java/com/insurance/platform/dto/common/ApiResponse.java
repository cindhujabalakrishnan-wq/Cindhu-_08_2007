package com.insurance.platform.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Standard API response envelope for all controller responses.
 *
 * @param <T> payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;

    /** Creates an empty response stamped with the current time. */
    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    /**
     * Creates a fully populated response.
     *
     * @param success whether the request succeeded
     * @param message human-readable message
     * @param data response payload
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    /**
     * Builds a successful response with data.
     *
     * @param data payload
     * @param <T> payload type
     * @return successful response
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Builds a successful response with a custom message.
     *
     * @param message human-readable message
     * @param data payload
     * @param <T> payload type
     * @return successful response
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Builds a successful response without data.
     *
     * @param message human-readable message
     * @param <T> payload type
     * @return successful response
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Builds a 201-style creation response with data.
     *
     * @param message human-readable message
     * @param data payload
     * @param <T> payload type
     * @return successful creation response
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Builds a 201-style creation response without data.
     *
     * @param message human-readable message
     * @param <T> payload type
     * @return successful creation response
     */
    public static <T> ApiResponse<T> created(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Builds an error response.
     *
     * @param message human-readable error message
     * @param <T> payload type
     * @return failure response
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Builds an error response.
     *
     * @param message human-readable error message
     * @param <T> payload type
     * @return failure response
     */
    public static <T> ApiResponse<T> error(String message) {
        return fail(message);
    }

    /**
     * Returns whether the request succeeded.
     *
     * @return success flag
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success flag.
     *
     * @param success success flag
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Returns the human-readable message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the human-readable message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the payload.
     *
     * @return payload
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the payload.
     *
     * @param data payload
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Returns the response timestamp.
     *
     * @return timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the response timestamp.
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
