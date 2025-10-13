package com.vertyll.fastprod.file.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @InjectMocks private FileStorageServiceImpl fileStorageService;

    private String tempDir;
    private MultipartFile testFile;
    private static final String TEST_USER_ID = "123";

    @BeforeEach
    void setUp() throws IOException {
        // Tworzenie tymczasowego katalogu dla testów
        tempDir = Files.createTempDirectory("file-test").toString();
        ReflectionTestUtils.setField(fileStorageService, "fileUploadPath", tempDir);

        // Przygotowanie testowego pliku
        testFile =
                new MockMultipartFile(
                        "test-file.txt", "test-file.txt", "text/plain", "test content".getBytes());
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
                new MockMultipartFile("test", "", "text/plain", "test content".getBytes());

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
        Path userDir = Paths.get(tempDir, "users", TEST_USER_ID);
        assertTrue(Files.exists(userDir));
        assertTrue(Files.isDirectory(userDir));
    }

    @Test
    void saveFile_WithSpecialCharactersInFileName_ShouldHandleCorrectly() {
        // given
        MultipartFile fileWithSpecialChars =
                new MockMultipartFile(
                        "test-file@#$%.txt",
                        "test-file@#$%.txt", "text/plain", "test content".getBytes());

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
                        "testfile", "testfile", "text/plain", "test content".getBytes());

        // when
        String resultPath = fileStorageService.saveFile(fileWithoutExtension, TEST_USER_ID);

        // then
        assertNotNull(resultPath);
        assertTrue(new File(resultPath).exists());
    }
}
