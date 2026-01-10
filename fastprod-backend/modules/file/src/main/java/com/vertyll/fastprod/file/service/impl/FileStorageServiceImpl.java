package com.vertyll.fastprod.file.service.impl;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

import com.google.common.base.Ascii;
import com.vertyll.fastprod.file.config.FileUploadProperties;
import com.vertyll.fastprod.file.service.FileStorageService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
class FileStorageServiceImpl implements FileStorageService {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public String saveFile(@Nonnull MultipartFile sourceFile, @Nonnull String userId) {
        final String fileUploadSubPath = "users" + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    @SuppressFBWarnings(
            value = "PATH_TRAVERSAL_IN",
            justification = "Path traversal is prevented by normalize() and startsWith() validation")
    private String uploadFile(@Nonnull MultipartFile sourceFile,
                              @Nonnull String fileUploadSubPath) {

        try {
            // Get the base directory and normalize it
            Path baseDir = Paths.get(fileUploadProperties.fileOutputPath())
                    .toRealPath()
                    .normalize();

            // Resolve the user directory and normalize it
            Path userDir = baseDir.resolve(fileUploadSubPath).normalize();

            // SECURITY: Prevent path traversal attacks
            // Ensure the resolved path is still within the base directory
            if (!userDir.startsWith(baseDir)) {
                log.warn("Blocked path traversal attempt: {}", fileUploadSubPath);
                return "";
            }

            // Create the directory if it doesn't exist
            Files.createDirectories(userDir);

            // Generate a safe filename
            String extension = getFileExtension(sourceFile.getOriginalFilename());
            String fileName = currentTimeMillis() + "." + extension;

            // Resolve the final file path and normalize it
            Path targetPath = userDir.resolve(fileName).normalize();

            // Write the file
            Files.write(targetPath, sourceFile.getBytes());

            log.info("File saved to: {}", targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            log.error("File was not saved", e);
            return "";
        }
    }


    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return Ascii.toLowerCase(fileName.substring(lastDotIndex + 1));
    }
}