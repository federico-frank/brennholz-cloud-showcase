package com.baumexpert.brennholzdemo.storage;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

/**
 * Liefert gespeicherte Bilder aus – lokal wie aus S3 über dieselbe URL (ADR-0003).
 * Schlüssel sind eindeutig (UUID), deshalb dürfen Browser die Bilder lange cachen.
 */
@Controller
public class StoredFileController {

    private final FileStorageService fileStorageService;

    public StoredFileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/bilder/{key}")
    public ResponseEntity<byte[]> image(@PathVariable String key) {
        return fileStorageService.load(key)
                .map(file -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(file.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .body(file.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
