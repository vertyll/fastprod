package com.vertyll.fastprod.translation;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/translations")
@RequiredArgsConstructor
class TranslationPublicController {

    private final TranslationService service;

    @GetMapping("/{language}")
    Map<String, String> messages(@PathVariable String language) {
        return service.messages(language);
    }
}
