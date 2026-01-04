package com.vertyll.fastprod.file.service;

import jakarta.annotation.Nonnull;
import org.springframework.web.multipart.MultipartFile;

@FunctionalInterface
public interface FileStorageService {
    String saveFile(@Nonnull MultipartFile sourceFile, @Nonnull String userId);
}
