package com.backend.couriersyncfeat4.exceptions;

/**
 * Vocabulario canonico de codigos de error. Centraliza los codigos que se
 * devuelven al cliente para evitar strings sueltos en el codigo.
 */
public enum ErrorCodes {
    INTERNAL_ERROR("INTERNAL_ERROR", 500, "Internal server error"),
    INVALID_INPUT("INVALID_INPUT", 400, "Invalid input"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", 404, "Resource not found"),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Unauthorized"),
    FORBIDDEN("FORBIDDEN", 403, "Forbidden"),
    CONFLICT("CONFLICT", 409, "Conflict"),

    PACKAGE_NOT_FOUND("PACKAGE_NOT_FOUND", 404, "Package not found"),
    PLACE_NOT_FOUND("PLACE_NOT_FOUND", 404, "Place not found"),
    PACKAGE_STATUS_NOT_FOUND("PACKAGE_STATUS_NOT_FOUND", 404, "Package status not found"),
    PACKAGE_NOT_UPDATABLE("PACKAGE_NOT_UPDATABLE", 409, "Package not updatable"),
    PACKAGE_NOT_CANCELLABLE("PACKAGE_NOT_CANCELLABLE", 409, "Package not cancellable"),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", 409, "Invalid status transition"),
    SAME_VALUE("SAME_VALUE", 409, "The two values are the same, must be different"),

    USER_NOT_FOUND("USER_NOT_FOUND", 404, "User not found");

    private final String code;
    private final int statusCode;
    private final String message;

    ErrorCodes(String code, int statusCode, String message) {
        this.code = code;
        this.statusCode = statusCode;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
