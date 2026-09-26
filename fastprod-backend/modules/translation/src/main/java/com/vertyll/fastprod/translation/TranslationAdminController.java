package com.vertyll.fastprod.translation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/translations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class TranslationAdminController {

    private final TranslationService service;

    @GetMapping
    List<TranslationResponse> list() {
        return service.findAll();
    }

    @PutMapping("/{key}")
    TranslationResponse update(@PathVariable String key, @Valid @RequestBody TranslationRequest request) {
        return service.update(key, request.messages());
    }

    @DeleteMapping("/{key}/customization")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@PathVariable String key) {
        service.reset(key);
    }
}
