package com.vertyll.fastprod.translation;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {
    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-02-01T10:00:00Z");
    private static final String KEY = "greeting";
    private static final LocalizedText DEFAULTS = new LocalizedText("Witaj {name}", "Hello {name}");

    @Mock
    private TranslationRepository repository;

    private TranslationService service;

    @BeforeEach
    void aServiceAtAFixedTime() {
        service = new TranslationService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private TranslationEntity stored() {
        TranslationEntity entity = new TranslationEntity(KEY, DEFAULTS, CREATED);
        when(repository.findById(KEY)).thenReturn(Optional.of(entity));
        return entity;
    }

    @Test
    void messagesAreGivenInTheLanguageAskedFor() {
        when(repository.findAll()).thenReturn(List.of(new TranslationEntity(KEY, DEFAULTS, CREATED)));

        assertEquals(Map.of(KEY, "Hello {name}"), service.messages("en"));
    }

    @Test
    void aLanguageTheApplicationDoesNotSpeakIsNotFound() {
        ApiException exception = assertThrows(ApiException.class, () -> service.messages("xx"));

        assertEquals(TranslationService.LANGUAGE_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void translationsAreListedByKey() {
        when(repository.findAll(Sort.by("key"))).thenReturn(List.of(new TranslationEntity(KEY, DEFAULTS, CREATED)));

        TranslationResponse response = service.findAll().getFirst();

        assertEquals(KEY, response.key());
        assertEquals(DEFAULTS, response.messages());
        assertEquals(DEFAULTS, response.defaults());
        assertFalse(response.customized());
        assertEquals(CREATED, response.updatedAt());
    }

    @Test
    void anEditedMessageIsKeptAndMarkedCustomized() {
        stored();
        LocalizedText edited = new LocalizedText("Cześć {name}", "Hi {name}");

        TranslationResponse response = service.update(KEY, edited);

        assertEquals(edited, response.messages());
        assertEquals(DEFAULTS, response.defaults());
        assertTrue(response.customized());
        assertEquals(NOW, response.updatedAt());
    }

    @Test
    void aMessageThatIsNoValidIcuIsRefused() {
        stored();
        LocalizedText broken = new LocalizedText("Witaj {name", "Hello {name}");

        ApiException exception = assertThrows(ApiException.class, () -> service.update(KEY, broken));

        assertEquals(TranslationService.INVALID_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void aPlaceholderTheDefaultDoesNotHaveIsRefused() {
        stored();
        LocalizedText unknown = new LocalizedText("Witaj {name}", "Hello {name} {surname}");

        ApiException exception = assertThrows(ApiException.class, () -> service.update(KEY, unknown));

        assertEquals(TranslationService.UNKNOWN_PLACEHOLDERS, exception.getMessage());
    }

    @Test
    void aRefusedEditChangesNothing() {
        TranslationEntity entity = stored();
        LocalizedText broken = new LocalizedText("Witaj {name", "Hello {name}");

        assertThrows(ApiException.class, () -> service.update(KEY, broken));

        assertEquals(DEFAULTS, entity.messages());
        assertFalse(entity.isCustomized());
    }

    @Test
    void aMissingTranslationCannotBeEdited() {
        when(repository.findById(KEY)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> service.update(KEY, DEFAULTS));

        assertEquals(TranslationService.TRANSLATION_NOT_FOUND, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void resettingBringsTheDefaultsBack() {
        TranslationEntity entity = stored();
        service.update(KEY, new LocalizedText("Cześć {name}", "Hi {name}"));

        service.reset(KEY);

        assertEquals(DEFAULTS, entity.messages());
        assertFalse(entity.isCustomized());
        assertEquals(NOW, entity.getUpdatedAt());
    }

    @Test
    void synchronizingAddsWhatIsNew() {
        when(repository.findAll()).thenReturn(List.of());
        ArgumentCaptor<TranslationEntity> saved = ArgumentCaptor.captor();

        service.synchronize(Map.of(KEY, DEFAULTS));

        verify(repository).save(saved.capture());
        assertEquals(KEY, saved.getValue().getKey());
        assertEquals(NOW, saved.getValue().getUpdatedAt());
    }

    @Test
    void synchronizingDeletesWhatTheCatalogueNoLongerHas() {
        TranslationEntity retired = new TranslationEntity("retired", DEFAULTS, CREATED);
        when(repository.findAll()).thenReturn(List.of(retired));

        service.synchronize(Map.of());

        ArgumentCaptor<Iterable<TranslationEntity>> deleted = ArgumentCaptor.captor();
        verify(repository).deleteAll(deleted.capture());
        List<TranslationEntity> entities = new ArrayList<>();
        deleted.getValue().forEach(entities::add);
        assertEquals(List.of(retired), entities);
    }

    @Test
    void synchronizingRefreshesTheDefaultsOfWhatStays() {
        TranslationEntity entity = new TranslationEntity(KEY, DEFAULTS, CREATED);
        when(repository.findAll()).thenReturn(List.of(entity));
        LocalizedText newDefaults = new LocalizedText("Dzień dobry {name}", "Good day {name}");

        service.synchronize(Map.of(KEY, newDefaults));

        assertEquals(newDefaults, entity.defaults());
        assertEquals(newDefaults, entity.messages());
        verify(repository, never()).save(any());
    }

    @Test
    void synchronizingKeepsWhatAnAdministratorEdited() {
        TranslationEntity entity = new TranslationEntity(KEY, DEFAULTS, CREATED);
        LocalizedText edited = new LocalizedText("Cześć {name}", "Hi {name}");
        entity.customize(edited, CREATED);
        when(repository.findAll()).thenReturn(List.of(entity));
        LocalizedText newDefaults = new LocalizedText("Dzień dobry {name}", "Good day {name}");

        service.synchronize(Map.of(KEY, newDefaults));

        assertEquals(edited, entity.messages());
        assertEquals(newDefaults, entity.defaults());
        assertTrue(entity.isCustomized());
    }

    @Test
    void anEditEqualToTheDefaultsIsNoCustomization() {
        TranslationEntity entity = new TranslationEntity(KEY, DEFAULTS, CREATED);

        entity.customize(DEFAULTS, NOW);

        assertFalse(entity.isCustomized());
    }
}
