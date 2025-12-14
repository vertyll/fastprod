package com.vertyll.fastprod.file.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import com.vertyll.fastprod.file.config.FileUploadProperties;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    private FileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    private MultipartFile testFile;
    private static final String TEST_USER_ID = "123";

    @BeforeEach
    void setUp() {
        // Create FileUploadProperties with the temp directory
        FileUploadProperties properties = new FileUploadProperties(tempDir.toString());

        // Create service instance with properties
        fileStorageService = new FileStorageServiceImpl(properties);

        // Prepare test file
        testFile = new MockMultipartFile(
                "test-file.txt",
                "test-file.txt",
                "text/plain",
                "test content".getBytes(UTF_8)
        );
    }

    @Test
    void saveFile_WhenValidFile_ShouldSaveAndReturnPath() {
        // when
        String resultPath = fileStorageService.saveFile(testFile, TEST_USER_ID);

        // then
        assertNotNull(resultPath);
        assertTrue(resultPath.contains(TEST_USER_ID));
        assertTrue(new File(resultPath).exists());
    }

    @Test
    void saveFile_WhenEmptyFileName_ShouldSaveWithOnlyDot() {
        // given
        MultipartFile fileWithoutName =
                new MockMultipartFile(
                        "test",
                        "",
                        "text/plain",
                        "test content".getBytes(UTF_8)
                );

        // when
        String resultPath = fileStorageService.saveFile(fileWithoutName, TEST_USER_ID);

        // then
        assertNotNull(resultPath);
        assertTrue(resultPath.contains(TEST_USER_ID));
        assertTrue(resultPath.matches(".*\\d+\\.$"));
        assertTrue(new File(resultPath).exists());
    }

    @Test
    void saveFile_ShouldCreateUserDirectory() {
        // when
        fileStorageService.saveFile(testFile, TEST_USER_ID);

        // then
        Path userDir = Paths.get(tempDir.toString(), "users", TEST_USER_ID);
        assertTrue(Files.exists(userDir));
        assertTrue(Files.isDirectory(userDir));
    }

    @Test
    void saveFile_WithSpecialCharactersInFileName_ShouldHandleCorrectly() {
        // given
        MultipartFile fileWithSpecialChars =
                new MockMultipartFile(
                        "test-file@#$%.txt",
                        "test-file@#$%.txt",
                        "text/plain",
                        "test content".getBytes(UTF_8)
                );

        // when
        String resultPath = fileStorageService.saveFile(fileWithSpecialChars, TEST_USER_ID);

        // then
        assertNotNull(resultPath);
        assertTrue(resultPath.contains(".txt"));
        assertTrue(new File(resultPath).exists());
    }

    @Test
    void saveFile_WithoutExtension_ShouldSaveSuccessfully() {
        // given
        MultipartFile fileWithoutExtension =
                new MockMultipartFile(
                        "testfile",
                        "testfile",
                        "text/plain",
                        "test content".getBytes(UTF_8)
                );

        // when
        String resultPath = fileStorageService.saveFile(fileWithoutExtension, TEST_USER_ID);

        // then
        assertNotNull(resultPath);
        assertTrue(new File(resultPath).exists());
    }
}