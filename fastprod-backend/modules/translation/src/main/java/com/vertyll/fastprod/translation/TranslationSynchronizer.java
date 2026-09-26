package com.vertyll.fastprod.translation;

import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
class TranslationSynchronizer implements ApplicationRunner {

    private final TranslationService service;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, LocalizedText> defaults = DefaultTranslations.load(objectMapper);
        service.synchronize(defaults);
        log.info("Translations synchronized: {} keys", defaults.size());
    }
}
