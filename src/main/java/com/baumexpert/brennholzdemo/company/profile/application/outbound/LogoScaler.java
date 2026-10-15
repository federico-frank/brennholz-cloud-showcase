package com.baumexpert.brennholzdemo.company.profile.application.outbound;

import com.baumexpert.brennholzdemo.company.profile.domain.LogoNotReadableException;
import com.baumexpert.brennholzdemo.storage.StoredFile;

/**
 * Verkleinert ein Logo für die Anzeige (F-043 S7).
 */
public interface LogoScaler {

    /**
     * Höchstens 400 × 400 px, Seitenverhältnis und Format bleiben, Metadaten (z. B. EXIF-Standort) entfallen.
     *
     * @throws LogoNotReadableException wenn die Bytes kein lesbares Bild sind
     */
    StoredFile scale(StoredFile original);
}
