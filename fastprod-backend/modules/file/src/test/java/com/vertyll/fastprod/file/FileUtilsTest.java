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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FileUtilsTest {

    private static final String TEST_CONTENT = "test content";

    @TempDir @Nullable Path tempDir;

    private Path testFile;

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
    void readFileFromLocation_WhenFileDoesNotExist_ShouldReturnEmptyArray() {
        assertNotNull(tempDir, "TempDir should be initialized by JUnit");

        // when
        byte[] result =
                FileUtils.readFileFromLocation(tempDir.resolve("non-existent.txt").toString());

        // then
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void readFileFromLocation_WhenPathIsNullOrBlank_ShouldReturnEmptyArray(String path) {
        // when
        byte[] result = FileUtils.readFileFromLocation(path);

        // then
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void readFileFromLocation_WhenDirectoryInsteadOfFile_ShouldReturnEmptyArray()
            throws IOException {
        assertNotNull(tempDir, "TempDir should be initialized by JUnit");

        // given
        Path testDir = tempDir.resolve("testDir");
        Files.createDirectory(testDir);

        // when
        byte[] result = FileUtils.readFileFromLocation(testDir.toString());

        // then
        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
