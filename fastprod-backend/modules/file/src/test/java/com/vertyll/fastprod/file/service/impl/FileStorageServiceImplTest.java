package com.vertyll.fastprod.file.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.vertyll.fastprod.file.config.FileUploadProperties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceImplTest {
    private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);

    @TempDir
    private Path uploads;

    private FileStorageServiceImpl storage() {
        return new FileStorageServiceImpl(new FileUploadProperties(uploads.toString()));
    }

    private static MockMultipartFile file(String name) {
        return new MockMultipartFile("file", name, "image/png", CONTENT);
    }

    @Test
    void aFileIsStoredInTheDirectoryOfItsUser() throws IOException {
        Path saved = Path.of(storage().saveFile(file("avatar.png"), "42"));

        assertTrue(saved.startsWith(uploads.toRealPath().resolve("users/42")));
        assertArrayEquals(CONTENT, Files.readAllBytes(saved));
    }

    @Test
    void theExtensionIsLowercased() {
        assertTrue(storage().saveFile(file("AVATAR.PNG"), "42").endsWith(".png"));
    }

    @Test
    void aNameWithoutAnExtensionLeavesTheExtensionEmpty() {
        assertTrue(storage().saveFile(file("avatar"), "42").endsWith("."));
    }

    @Test
    void aFileWithoutANameLeavesTheExtensionEmpty() {
        assertTrue(storage().saveFile(file(""), "42").endsWith("."));
    }

    @Test
    void aUserIdThatLeavesTheUploadsDirectoryStoresNothing() throws IOException {
        assertEquals("", storage().saveFile(file("avatar.png"), "../.."));

        try (Stream<Path> entries = Files.list(uploads)) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void anUploadsDirectoryThatDoesNotExistStoresNothing() {
        FileStorageServiceImpl storage =
                new FileStorageServiceImpl(new FileUploadProperties(uploads.resolve("missing").toString()));

        assertEquals("", storage.saveFile(file("avatar.png"), "42"));
    }
}
