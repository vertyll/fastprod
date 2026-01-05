package com.vertyll.fastprod.email.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account"),
    CHANGE_EMAIL("change_email"),
    CHANGE_PASSWORD("change_password"),
    RESET_PASSWORD("reset_password");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
