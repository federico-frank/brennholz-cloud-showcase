package com.baumexpert.brennholzdemo.storage;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Port für den Bildspeicher. Implementierungen: lokal ({@code storage.type=local}) oder S3 ({@code storage.type=s3}).
 * Ausgeliefert werden Bilder immer über {@code /bilder/{key}} (ADR-0003).
 */
public interface FileStorageService {

    /**
     * Lädt eine Datei hoch und gibt den eindeutigen Dateipfad (Key) zurück.
     */
    String uploadFile(MultipartFile file);

    /**
     * Speichert bereits verarbeitete Bytes (z. B. ein verkleinertes Logo) unter einem neuen Schlüssel.
     */
    String store(byte[] content, String contentType);

    /**
     * Löscht eine Datei anhand ihres Schlüssels.
     */
    void deleteFile(String fileKey);

    /**
     * Lädt eine Datei; leer, wenn es sie nicht gibt oder der Schlüssel ungültig ist.
     */
    Optional<StoredFile> load(String fileKey);

    /**
     * Abrufbare URL für das Frontend – für beide Speicher dieselbe App-URL.
     */
    default String getFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        return "/bilder/" + UriUtils.encodePathSegment(fileKey, StandardCharsets.UTF_8);
    }
}
