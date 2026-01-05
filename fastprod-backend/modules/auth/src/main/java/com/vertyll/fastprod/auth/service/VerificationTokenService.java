package com.vertyll.fastprod.auth.service;

import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.user.entity.User;

import jakarta.transaction.Transactional;

public interface VerificationTokenService {
    @Transactional
    String createVerificationToken(
            User user, VerificationTokenType tokenType, String additionalData);

    @Transactional
    VerificationToken getValidToken(String code, VerificationTokenType expectedType);

    @Transactional
    void markTokenAsUsed(VerificationToken token);

    @Transactional
    void cleanupExpiredTokens();
}
