package com.vertyll.fastprod.role.enums;

public enum RoleType {
    ADMIN,
    USER,
    MANAGER,
    EMPLOYEE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}