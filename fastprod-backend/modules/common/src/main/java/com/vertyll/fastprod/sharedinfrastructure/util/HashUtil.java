package com.vertyll.fastprod.sharedinfrastructure.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HashUtil {
    private static final String SHA_256_ALGORITHM_NOT_AVAILABLE = "SHA-256 algorithm not available";
    private static final String ALGORITHM = "SHA-256";

    private HashUtil() {
    }

    public static String hashToken(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available {e}", e);
            throw new IllegalStateException(SHA_256_ALGORITHM_NOT_AVAILABLE, e);
        }
    }
}
