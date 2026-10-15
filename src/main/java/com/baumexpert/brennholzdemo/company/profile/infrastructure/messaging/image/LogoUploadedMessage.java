package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.image;

/**
 * Nachricht an den Logo-Worker (Claim-Check): nur der Schlüssel, die Bilddaten liegen im Speicher.
 */
public record LogoUploadedMessage(Long companyId, String originalKey) {
}
