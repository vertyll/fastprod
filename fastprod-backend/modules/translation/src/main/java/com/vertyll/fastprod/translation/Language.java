package com.vertyll.fastprod.translation;

import java.util.Arrays;
import java.util.Optional;

public enum Language {
    PL("pl"),
    EN("en");

    public static final Language DEFAULT = PL;

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<Language> fromCode(String code) {
        return Arrays.stream(values()).filter(language -> language.code.equals(code)).findFirst();
    }
}
