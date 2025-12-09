package com.vertyll.fastprod.shared.security;

public enum RoleType {
    ADMIN,
    USER,
    MANAGER,
    EMPLOYEE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
