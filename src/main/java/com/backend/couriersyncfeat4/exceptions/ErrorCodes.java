package com.backend.couriersyncfeat4.exceptions;

/**
 * ErrorCodes
 */
public enum ErrorCodes {
    INTERNAL_ERROR("INTERNAL_ERROR", 500),
    INVALID_INPUT("INVALID_INPUT", 400),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", 404),
    UNAUTHORIZED("UNAUTHORIZED", 401),
    FORBIDDEN("FORBIDDEN", 403),
    CONFLICT("CONFLICT", 409);

    private final String code;
    private final int statusCode;

    ErrorCodes(String code, int statusCode) {
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
