package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.auth.repository.VerificationTokenRepository;
import com.vertyll.fastprod.auth.service.VerificationTokenService;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
class VerificationTokenServiceImpl implements VerificationTokenService {

    private static final Random RANDOM = new Random();

    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * Creates a verification token for a user.
     */
    @Override
    @Transactional
    public String createVerificationToken(User user, VerificationTokenType tokenType, String additionalData) {
        String code = generateVerificationCode();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(code)
                .user(user)
                .expiryDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24))
                .used(false)
                .tokenType(tokenType)
                .additionalData(additionalData)
                .build();

        verificationTokenRepository.save(verificationToken);

        log.info("Created verification token for user: {} with type: {}",
                user.getEmail(), tokenType);

        return code;
    }

    /**
     * Validates and retrieves a verification token.
     */
    @Override
    @Transactional(readOnly = true)
    public VerificationToken getValidToken(String code, VerificationTokenType expectedType) {
        VerificationToken token = verificationTokenRepository.findByToken(code)
                .orElseThrow(() -> new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST));

        if (token.isUsed()) {
            throw new ApiException("Verification code already used", HttpStatus.BAD_REQUEST);
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiException("Verification code expired", HttpStatus.BAD_REQUEST);
        }

        if (token.getTokenType() != expectedType) {
            throw new ApiException("Invalid verification code type", HttpStatus.BAD_REQUEST);
        }

        return token;
    }

    /**
     * Marks a token as used.
     */
    @Override
    @Transactional
    public void markTokenAsUsed(VerificationToken token) {
        token.setUsed(true);
        verificationTokenRepository.save(token);

        log.info("Marked verification token as used for user: {}", token.getUser().getEmail());
    }

    /**
     * Scheduled task to delete expired tokens.
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = verificationTokenRepository.deleteByExpiryDateBeforeAndIsUsed(LocalDateTime.now(ZoneOffset.UTC), true);
        log.info("Cleaned up {} expired verification tokens", deleted);
    }

    private String generateVerificationCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
