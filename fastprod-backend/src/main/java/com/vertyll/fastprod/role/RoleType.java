package com.vertyll.fastprod.role;

public enum RoleType {
    ADMIN,
    USER,
    MANAGER,
    EMPLOYEE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}