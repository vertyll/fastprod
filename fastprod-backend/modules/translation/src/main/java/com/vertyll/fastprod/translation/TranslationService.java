package com.vertyll.fastprod.translation;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class TranslationService {

    static final String TRANSLATION_NOT_FOUND = "errors.translation.notFound";
    static final String LANGUAGE_NOT_FOUND = "errors.translation.languageNotFound";
    static final String INVALID_MESSAGE = "errors.translation.invalidMessage";
    static final String UNKNOWN_PLACEHOLDERS = "errors.translation.unknownPlaceholders";

    private final TranslationRepository repository;
    private final Clock clock;

    @Transactional(readOnly = true)
    Map<String, String> messages(String languageCode) {
        Language language = Language.fromCode(languageCode)
            .orElseThrow(() -> new ApiException(LANGUAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
        return repository.findAll()
            .stream()
            .collect(
                Collectors.toMap(
                    TranslationEntity::getKey,
                    entity -> entity.messages().in(language),
                    (first, _) -> first,
                    TreeMap::new
                )
            );
    }

    @Transactional(readOnly = true)
    List<TranslationResponse> findAll() {
        return repository.findAll(Sort.by("key")).stream().map(TranslationResponse::of).toList();
    }

    @Transactional
    TranslationResponse update(String key, LocalizedText messages) {
        TranslationEntity entity = find(key);
        for (Language language : Language.values()) {
            String message = messages.in(language);
            if (!IcuMessages.isValid(message)) {
                throw new ApiException(INVALID_MESSAGE, HttpStatus.BAD_REQUEST, Map.of("language", language.code()));
            }
            Set<String> unknown = new TreeSet<>(IcuMessages.placeholders(message));
            unknown.removeAll(IcuMessages.placeholders(entity.defaults().in(language)));
            if (!unknown.isEmpty()) {
                throw new ApiException(
                    UNKNOWN_PLACEHOLDERS,
                    HttpStatus.BAD_REQUEST,
                    Map.of("language", language.code(), "placeholders", String.join(", ", unknown))
                );
            }
        }
        entity.customize(messages, Instant.now(clock));
        return TranslationResponse.of(entity);
    }

    @Transactional
    void reset(String key) {
        find(key).reset(Instant.now(clock));
    }

    @Transactional
    void synchronize(Map<String, LocalizedText> defaults) {
        Instant now = Instant.now(clock);
        Map<String, TranslationEntity> existing =
                repository.findAll().stream().collect(Collectors.toMap(TranslationEntity::getKey, entity -> entity));
        defaults.forEach((key, text) -> {
            TranslationEntity entity = existing.remove(key);
            if (entity == null) {
                repository.save(new TranslationEntity(key, text, now));
            } else {
                entity.refreshDefaults(text);
            }
        });
        repository.deleteAll(existing.values());
    }

    private TranslationEntity find(String key) {
        return repository.findById(key)
            .orElseThrow(() -> new ApiException(TRANSLATION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
