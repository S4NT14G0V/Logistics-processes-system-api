package com.backend.couriersyncfeat4.enums;

public enum PackageStatusEnum {
    CREATED("CREATED", "Created"),
    IN_TRANSIT("IN_TRANSIT", "In Transit"),
    DELIVERED("DELIVERED", "Delivered"),
    CANCELLED("CANCELLED", "Cancelled");

    private final String code;
    private final String name;

    PackageStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PackageStatusEnum fromCode(String code) {
        for (PackageStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public boolean canTransitionTo(PackageStatusEnum target) {
        return switch (this) {
            case CREATED -> target == IN_TRANSIT || target == CANCELLED;
            case IN_TRANSIT -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
