package com.backend.couriersyncfeat4.config;

/**
 * Vocabulario canónico de permisos. El mapeo rol -> permiso vive en la base de
 * datos (tabla {@code role_permission}); aquí solo se centralizan los nombres
 * para evitar strings sueltos en el código. Deben coincidir con
 * {@code permission.name}.
 */
public enum Permission {

    PACKAGE_CREATE("package:create"),
    PACKAGE_READ_ALL("package:read:all"),
    PACKAGE_READ_OWN("package:read:own"),
    PACKAGE_UPDATE("package:update"),
    PACKAGE_CANCEL("package:cancel"),

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
