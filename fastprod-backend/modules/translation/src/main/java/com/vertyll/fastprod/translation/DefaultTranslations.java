package com.vertyll.fastprod.translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

final class DefaultTranslations {

    private static final TypeReference<Map<String, String>> MESSAGES = new TypeReference<>() {
    };

    private DefaultTranslations() {
    }

    static Map<String, LocalizedText> load(ObjectMapper mapper) {
        Map<Language, Map<String, String>> byLanguage = new EnumMap<>(Language.class);
        for (Language language : Language.values()) {
            byLanguage.put(language, read(mapper, language));
        }
        Set<String> keys = new TreeSet<>(catalogue(byLanguage, Language.DEFAULT).keySet());
        byLanguage.forEach((language, messages) -> {
            if (!messages.keySet().equals(keys)) {
                Set<String> difference = new TreeSet<>(keys);
                difference.removeAll(messages.keySet());
                Set<String> extra = new TreeSet<>(messages.keySet());
                extra.removeAll(keys);
                difference.addAll(extra);
                throw new IllegalStateException("Translations " + language.code() + " differ in keys: " + difference);
            }
        });
        Map<String, LocalizedText> translations = new TreeMap<>();
        for (String key : keys) {
            LocalizedText text =
                    new LocalizedText(message(byLanguage, Language.PL, key), message(byLanguage, Language.EN, key));
            for (Language language : Language.values()) {
                if (!IcuMessages.isValid(text.in(language))) {
                    throw new IllegalStateException("Invalid ICU message " + key + " (" + language.code() + ")");
                }
            }
            translations.put(key, text);
        }
        return translations;
    }

    private static Map<String, String> catalogue(Map<Language, Map<String, String>> byLanguage, Language language) {
        Map<String, String> messages = byLanguage.get(language);
        if (messages == null) {
            throw new IllegalStateException("Missing translations " + language.code());
        }
        return messages;
    }

    private static String message(Map<Language, Map<String, String>> byLanguage, Language language, String key) {
        String message = catalogue(byLanguage, language).get(key);
        if (message == null || message.isBlank()) {
            throw new IllegalStateException("Missing translation " + key + " (" + language.code() + ")");
        }
        return message;
    }

    private static Map<String, String> read(ObjectMapper mapper, Language language) {
        ClassPathResource resource = new ClassPathResource("i18n/" + language.code() + ".json");
        try (InputStream input = resource.getInputStream()) {
            return mapper.readValue(input, MESSAGES);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + resource.getPath(), e);
        }
    }
}
