package com.baumexpert.brennholzdemo.storage;

/**
 * Inhalt und Typ einer gespeicherten Datei, unabhängig davon, ob sie lokal oder in S3 liegt.
 */
public record StoredFile(byte[] content, String contentType) {
}
