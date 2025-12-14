package com.vertyll.fastprod.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

    @TempDir Path tempDir;

    private Path testFile;
    private final String TEST_CONTENT = "test content";

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("test-file.txt");
        Files.write(testFile, TEST_CONTENT.getBytes());
    }

    @Test
    void readFileFromLocation_WhenFileExists_ShouldReturnContent() {
        // when
        byte[] result = FileUtils.readFileFromLocation(testFile.toString());

        // then
        assertNotNull(result);
        assertEquals(TEST_CONTENT, new String(result));
    }

    @Test
    void readFileFromLocation_WhenFileDoesNotExist_ShouldReturnNull() {
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
        // given
        Path testDir = tempDir.resolve("testDir");
        Files.createDirectory(testDir);

        // when
        byte[] result = FileUtils.readFileFromLocation(testDir.toString());

        // then
        assertNull(result);
    }
}
