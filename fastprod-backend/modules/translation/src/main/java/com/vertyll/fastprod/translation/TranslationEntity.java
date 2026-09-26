package com.vertyll.fastprod.translation;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;

@Getter
@Entity
@Table(name = "translation")
public class TranslationEntity {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "message_pl", nullable = false)
    private String messagePl;

    @Column(name = "message_en", nullable = false)
    private String messageEn;

    @Column(name = "default_pl", nullable = false)
    private String defaultPl;

    @Column(name = "default_en", nullable = false)
    private String defaultEn;

    @Column(nullable = false)
    private boolean customized;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @SuppressWarnings("NullAway.Init")
    protected TranslationEntity() {
    }

    TranslationEntity(String key, LocalizedText defaults, Instant now) {
        this.key = key;
        this.messagePl = defaults.pl();
        this.messageEn = defaults.en();
        this.defaultPl = defaults.pl();
        this.defaultEn = defaults.en();
        this.updatedAt = now;
    }

    LocalizedText messages() {
        return new LocalizedText(messagePl, messageEn);
    }

    LocalizedText defaults() {
        return new LocalizedText(defaultPl, defaultEn);
    }

    void refreshDefaults(LocalizedText newDefaults) {
        this.defaultPl = newDefaults.pl();
        this.defaultEn = newDefaults.en();
        if (!customized) {
            this.messagePl = newDefaults.pl();
            this.messageEn = newDefaults.en();
        }
    }

    void customize(LocalizedText messages, Instant now) {
        this.messagePl = messages.pl();
        this.messageEn = messages.en();
        this.customized = !messages.equals(defaults());
        this.updatedAt = now;
    }

    void reset(Instant now) {
        customize(defaults(), now);
    }
}
