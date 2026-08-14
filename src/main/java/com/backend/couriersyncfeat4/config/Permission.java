package com.backend.couriersyncfeat4.config;

/**
 * Vocabulario canónico de permisos. El mapeo rol -> permiso vive en la base de
 * datos (tabla {@code role_permission}); aquí solo se centralizan los nombres
 * para evitar strings sueltos en el código. Deben coincidir con
 * {@code permission.name}.
 */
public enum Permission {

    PACKAGE_CREATE_ALL("package:create:all"),
    PACKAGE_CREATE_OWN("package:create:own"),
    PACKAGE_READ_ALL("package:read:all"),
    PACKAGE_READ_OWN("package:read:own"),
    PACKAGE_UPDATE_ALL("package:update:all"),
    PACKAGE_UPDATE_OWN("package:update:own"),
    PACKAGE_CANCEL_ALL("package:cancel:all"),
    PACKAGE_CANCEL_OWN("package:cancel:own"),

    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete");

    private final String code;

    Permission(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
