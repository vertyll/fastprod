package com.vertyll.fastprod.shared.i18n;

import java.time.Instant;

public record TranslationRow(
    String key,
    LocalizedText messages,
    LocalizedText defaults,
    boolean customized,
    Instant updatedAt
) {
}
