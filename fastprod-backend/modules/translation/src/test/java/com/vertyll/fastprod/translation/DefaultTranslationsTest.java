package com.vertyll.fastprod.translation;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ibm.icu.text.MessageFormat;

import tools.jackson.databind.json.JsonMapper;

import static java.util.Objects.requireNonNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTranslationsTest {

    private static final Map<String, LocalizedText> CATALOGUE = DefaultTranslations.load(JsonMapper.builder().build());

    @Test
    void loadsBothLanguagesWithTheSameKeysAndValidIcu() {
        assertFalse(CATALOGUE.isEmpty());
        CATALOGUE.forEach((key, text) -> {
            assertTrue(IcuMessages.isValid(text.pl()), key);
            assertTrue(IcuMessages.isValid(text.en()), key);
            assertEquals(IcuMessages.placeholders(text.pl()), IcuMessages.placeholders(text.en()), key);
        });
    }

    @Test
    void formatsPolishPlurals() {
        String pattern = requireNonNull(CATALOGUE.get("translations.count")).pl();
        Locale polish = Locale.forLanguageTag("pl");

        assertEquals("1 tłumaczenie", new MessageFormat(pattern, polish).format(Map.of("count", 1)));
        assertEquals("3 tłumaczenia", new MessageFormat(pattern, polish).format(Map.of("count", 3)));
        assertEquals("5 tłumaczeń", new MessageFormat(pattern, polish).format(Map.of("count", 5)));
    }

    @Test
    void findsPlaceholdersAndRejectsBrokenMessages() {
        assertEquals(Set.of("name", "count"), IcuMessages.placeholders("{name}: {count, plural, other {#}}"));
        assertFalse(IcuMessages.isValid("{count, plural, one {x}"));
    }
}
