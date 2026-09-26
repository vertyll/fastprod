package com.vertyll.fastprod.translation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TranslationRequest(@NotNull(message = "validation.translation.required") @Valid LocalizedText messages) {
}
