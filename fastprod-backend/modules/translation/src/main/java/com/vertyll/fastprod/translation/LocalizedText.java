package com.vertyll.fastprod.translation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalizedText(
    @NotBlank(
        message = "validation.translation.required"
    ) @Size(max = MAX_LENGTH, message = "validation.translation.tooLong") String pl,
    @NotBlank(
        message = "validation.translation.required"
    ) @Size(max = MAX_LENGTH, message = "validation.translation.tooLong") String en
) {

    static final int MAX_LENGTH = 2000;

    public String in(Language language) {
        return switch (language) {
            case PL -> pl;
            case EN -> en;
        };
    }
}
