package com.vertyll.fastprod.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@Slf4j
public final class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static byte[] readFileFromLocation(@Nullable String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return new byte[0];
        }

        try {
            Path filePath = new File(fileUrl).toPath();
            return Files.readAllBytes(filePath);
        } catch (IOException _) {
            log.warn("No file found in the path {}", fileUrl);
        }

        return new byte[0];
    }
}
