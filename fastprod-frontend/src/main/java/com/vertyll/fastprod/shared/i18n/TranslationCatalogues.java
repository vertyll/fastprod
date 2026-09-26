package com.vertyll.fastprod.shared.i18n;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.vertyll.fastprod.shared.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TranslationCatalogues {

    private static final Duration TIME_TO_LIVE = Duration.ofMinutes(5);

    private final TranslationClient client;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Catalogue> catalogues = new ConcurrentHashMap<>();

    public TranslationCatalogues(TranslationClient client) {
        this.client = client;
    }

    Map<String, String> messages(String language) {
        Instant now = clock.instant();
        return catalogues.compute(language, (_, current) -> {
            if (current != null && current.loadedAt().plus(TIME_TO_LIVE).isAfter(now)) {
                return current;
            }
            try {
                return new Catalogue(client.messages(language), now);
            } catch (ApiException e) {
                log.warn("Could not load translations for {}", language, e);
                return current != null ? current : new Catalogue(Map.of(), now.minus(TIME_TO_LIVE));
            }
        }).messages();
    }

    public void invalidate() {
        catalogues.clear();
    }

    private record Catalogue(Map<String, String> messages, Instant loadedAt) {
    }
}
