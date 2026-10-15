package com.baumexpert.brennholzdemo.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Bildspeicher auf der lokalen Festplatte – Standard für die Entwicklung (storage.type=local).
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileSystemStorageService implements FileStorageService {

    private final Path uploadDir;

    public LocalFileSystemStorageService(@Value("${storage.local.dir:uploads/profile-images}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new StorageException("Konnte Upload-Verzeichnis nicht erstellen", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String key = ImageUploadPolicy.newKey(file);
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(key), StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (IOException e) {
            throw new StorageException("Fehler beim lokalen Speichern der Datei", e);
        }
    }

    @Override
    public String store(byte[] content, String contentType) {
        String key = ImageUploadPolicy.newKey(contentType);
        try {
            Files.write(uploadDir.resolve(key), content);
            return key;
        } catch (IOException e) {
            throw new StorageException("Fehler beim lokalen Speichern der Datei", e);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        resolve(fileKey).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new StorageException("Fehler beim Löschen der lokalen Datei: " + fileKey, e);
            }
        });
    }

    @Override
    public Optional<StoredFile> load(String fileKey) {
        return resolve(fileKey)
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        String type = URLConnection.guessContentTypeFromName(path.getFileName().toString());
                        return new StoredFile(Files.readAllBytes(path), type != null ? type : "application/octet-stream");
                    } catch (IOException e) {
                        throw new StorageException("Fehler beim Lesen der lokalen Datei: " + fileKey, e);
                    }
                });
    }

    private Optional<Path> resolve(String fileKey) {
        if (!ImageUploadPolicy.isValidKey(fileKey)) {
            return Optional.empty();
        }
        Path path = uploadDir.resolve(fileKey).normalize();
        return path.startsWith(uploadDir) ? Optional.of(path) : Optional.empty();
    }
}
