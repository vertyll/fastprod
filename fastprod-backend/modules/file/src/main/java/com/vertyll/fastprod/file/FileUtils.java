package com.vertyll.fastprod.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static byte[] readFileFromLocation(@Nullable String fileUrl, Path baseDir) {

        if (StringUtils.isBlank(fileUrl)) {
            return new byte[0];
        }

        try {
            Path basePath = baseDir.toRealPath().normalize();
            Path resolvedPath = basePath.resolve(fileUrl).normalize();

            if (!resolvedPath.startsWith(basePath)) {
                log.warn("Blocked path traversal attempt: {}", fileUrl);
                return new byte[0];
            }

            return Files.readAllBytes(resolvedPath);
        } catch (IOException e) {
            log.warn("No file found in the path {}", fileUrl);
            return new byte[0];
        }
    }
}
