package com.vertyll.fastprod.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HashUtil {

    private static final String SHA_256_ALGORITHM_NOT_AVAILABLE = "SHA-256 algorithm not available";
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hashes a token using SHA-256 algorithm. This is a deterministic hash function - the same
     * input always produces the same output.
     *
     * @param value the value to hash
     * @return hexadecimal representation of the hash
     */
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
