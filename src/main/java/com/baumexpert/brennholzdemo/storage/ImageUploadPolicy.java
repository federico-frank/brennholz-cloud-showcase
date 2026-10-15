package com.baumexpert.brennholzdemo.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Gemeinsame Regeln für Bild-Uploads (F-040 S7): nur JPG und PNG,
 * Schlüssel = UUID + Endung – ohne Originalnamen, damit er in URLs und S3 unproblematisch ist.
 */
final class ImageUploadPolicy {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png");

    private ImageUploadPolicy() {
    }

    static String newKey(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Leere Datei kann nicht hochgeladen werden.");
        }
        return newKey(file.getContentType());
    }

    static String newKey(String contentType) {
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new StorageException("Ungültiges Dateiformat. Nur JPG und PNG sind erlaubt.");
        }
        return UUID.randomUUID() + "." + extension;
    }

    /** Ein Schlüssel ist ein einzelner Dateiname – keine Ordner, keine Pfad-Tricks. */
    static boolean isValidKey(String key) {
        return key != null && !key.isBlank() && !key.contains("/") && !key.contains("\\") && !key.contains("..");
    }
}
