package com.vertyll.fastprod.auth.service;

import jakarta.transaction.Transactional;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.user.entity.User;

public interface VerificationTokenService {
    @Transactional
    String createVerificationToken(User user, VerificationTokenType tokenType, @Nullable String additionalData);

    @Transactional
    VerificationToken getValidToken(String code, VerificationTokenType expectedType);

    @Transactional
    void markTokenAsUsed(VerificationToken token);

    @Transactional
    void cleanupExpiredTokens();
}
