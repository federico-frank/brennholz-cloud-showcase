package com.baumexpert.brennholzdemo.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * F-040 S7: Lokaler Speicher – sichere Schlüssel, Laden, Schutz vor Pfad-Tricks.
 */
class LocalFileSystemStorageServiceTest {

    @TempDir
    Path dir;

    @Test
    void speichertUnterUuidSchluesselUndLaedtWieder() {
        LocalFileSystemStorageService storage = new LocalFileSystemStorageService(dir.toString());

        String key = storage.uploadFile(new MockMultipartFile("file", "Mein Logo (1).PNG", "image/png", new byte[]{1, 2}));

        assertThat(key).matches("[0-9a-f-]{36}\\.png");
        StoredFile file = storage.load(key).orElseThrow();
        assertThat(file.content()).containsExactly(1, 2);
        assertThat(file.contentType()).isEqualTo("image/png");
    }

    @Test
    void speichertVerarbeiteteBytesUnterNeuemSchluessel() {
        LocalFileSystemStorageService storage = new LocalFileSystemStorageService(dir.toString());

        String key = storage.store(new byte[]{5, 6}, "image/jpeg");

        assertThat(key).matches("[0-9a-f-]{36}\\.jpg");
        assertThat(storage.load(key).orElseThrow().content()).containsExactly(5, 6);
    }

    @Test
    void lehntAndereDateitypenAb() {
        LocalFileSystemStorageService storage = new LocalFileSystemStorageService(dir.toString());

        assertThatThrownBy(() -> storage.uploadFile(new MockMultipartFile("file", "x.gif", "image/gif", new byte[]{1})))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void schluesselAusserhalbDesOrdnersWirdNichtGeladen() {
        LocalFileSystemStorageService storage = new LocalFileSystemStorageService(dir.toString());

        assertThat(storage.load("../geheim.txt")).isEmpty();
        assertThat(storage.load("unterordner/x.png")).isEmpty();
    }
}
