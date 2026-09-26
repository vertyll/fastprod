package com.vertyll.fastprod.translation;

import java.time.Instant;

public record TranslationResponse(
    String key,
    LocalizedText messages,
    LocalizedText defaults,
    boolean customized,
    Instant updatedAt
) {

    static TranslationResponse of(TranslationEntity entity) {
        return new TranslationResponse(
            entity.getKey(),
            entity.messages(),
            entity.defaults(),
            entity.isCustomized(),
            entity.getUpdatedAt()
        );
    }
}
