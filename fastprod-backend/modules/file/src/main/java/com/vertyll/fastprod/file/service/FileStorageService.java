package com.vertyll.fastprod.file.service;

import org.springframework.web.multipart.MultipartFile;

@FunctionalInterface
public interface FileStorageService {
    String saveFile(MultipartFile sourceFile, String userId);
}
