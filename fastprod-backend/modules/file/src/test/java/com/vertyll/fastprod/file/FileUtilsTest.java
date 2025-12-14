package com.vertyll.fastprod.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

    @TempDir
    @Nullable
    Path tempDir;

    private Path testFile;
    private final String TEST_CONTENT = "test content";

    @BeforeEach
    void setUp() throws IOException {
        assertNotNull(tempDir, "TempDir should be initialized by JUnit");
        testFile = tempDir.resolve("test-file.txt");
        Files.writeString(testFile, TEST_CONTENT);
    }

    @Test
    void readFileFromLocation_WhenFileExists_ShouldReturnContent() {
        // when
        byte[] result = FileUtils.readFileFromLocation(testFile.toString());

        // then
        assertNotNull(result);
        assertEquals(TEST_CONTENT, new String(result, UTF_8));
    }

    @Test
    void readFileFromLocation_WhenFileDoesNotExist_ShouldReturnNull() {
        assertNotNull(tempDir, "TempDir should be initialized by JUnit");

        // when
        byte[] result =
                FileUtils.readFileFromLocation(tempDir.resolve("non-existent.txt").toString());

        // then
        assertNull(result);
    }

    @Test
    void readFileFromLocation_WhenPathIsNull_ShouldReturnNull() {
        // when
        byte[] result = FileUtils.readFileFromLocation(null);

        // then
        assertNull(result);
    }

    @Test
    void readFileFromLocation_WhenPathIsEmpty_ShouldReturnNull() {
        // when
        byte[] result = FileUtils.readFileFromLocation("");

        // then
        assertNull(result);
    }

    @Test
    void readFileFromLocation_WhenPathIsBlank_ShouldReturnNull() {
        // when
        byte[] result = FileUtils.readFileFromLocation("   ");

        // then
        assertNull(result);
    }

    @Test
    void readFileFromLocation_WhenDirectoryInsteadOfFile_ShouldReturnNull() throws IOException {
        assertNotNull(tempDir, "TempDir should be initialized by JUnit");

        // given
        Path testDir = tempDir.resolve("testDir");
        Files.createDirectory(testDir);

        // when
        byte[] result = FileUtils.readFileFromLocation(testDir.toString());

        // then
        assertNull(result);
    }
}