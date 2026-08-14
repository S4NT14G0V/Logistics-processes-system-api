package com.backend.couriersyncfeat4.exceptions;

/**
 * Vocabulario canonico de codigos de error. Centraliza los codigos que se
 * devuelven al cliente para evitar strings sueltos en el codigo.
 */
public enum ErrorCodes {
    INTERNAL_ERROR("INTERNAL_ERROR", 500),
    INVALID_INPUT("INVALID_INPUT", 400),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", 404),
    UNAUTHORIZED("UNAUTHORIZED", 401),
    FORBIDDEN("FORBIDDEN", 403),
    CONFLICT("CONFLICT", 409),

    PACKAGE_NOT_FOUND("PACKAGE_NOT_FOUND", 404),
    PLACE_NOT_FOUND("PLACE_NOT_FOUND", 404),
    PACKAGE_STATUS_NOT_FOUND("PACKAGE_STATUS_NOT_FOUND", 404),
    PACKAGE_NOT_UPDATABLE("PACKAGE_NOT_UPDATABLE", 409),
    PACKAGE_NOT_CANCELLABLE("PACKAGE_NOT_CANCELLABLE", 409),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", 409);

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
