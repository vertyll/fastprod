package com.vertyll.fastprod.file.service;

import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Nonnull;

@FunctionalInterface
public interface FileStorageService {
    String saveFile(@Nonnull MultipartFile sourceFile, @Nonnull String userId);
}
