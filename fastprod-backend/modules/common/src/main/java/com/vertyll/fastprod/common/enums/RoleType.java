package com.vertyll.fastprod.common.enums;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN("ADMIN"),
    USER("USER"),
    MANAGER("MANAGER"),
    EMPLOYEE("EMPLOYEE");

    public static final String ROLE_PREFIX = "ROLE_";

    private static final String UNKNOWN_USER_ROLE = "Unknown user role: ";

    private final String value;

    private final String roleWithPrefix;

    RoleType(String value) {
        this.value = value;
        this.roleWithPrefix = ROLE_PREFIX + value.toLowerCase(Locale.ROOT);
    }

    public static RoleType fromValue(@Nullable String value) {
        if (value == null) {
            throw new IllegalArgumentException(UNKNOWN_USER_ROLE + "null");
        }

        for (RoleType userRoleEnum : values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }

        throw new IllegalArgumentException(UNKNOWN_USER_ROLE + value);
    }
}
