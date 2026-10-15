package com.baumexpert.brennholzdemo.company.profile.application;

import com.baumexpert.brennholzdemo.company.Company;
import com.baumexpert.brennholzdemo.company.CompanyRepository;
import com.baumexpert.brennholzdemo.company.profile.application.outbound.CompanyEventPublisher;
import com.baumexpert.brennholzdemo.company.profile.application.outbound.ImageEventPublisher;
import com.baumexpert.brennholzdemo.company.profile.application.outbound.LogoScaler;
import com.baumexpert.brennholzdemo.company.profile.domain.LogoNotReadableException;
import com.baumexpert.brennholzdemo.company.profile.domain.LogoStatus;
import com.baumexpert.brennholzdemo.storage.FileStorageService;
import com.baumexpert.brennholzdemo.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Logo hochladen und asynchron verkleinern (F-043 S7, ADR-0003).
 * <ol>
 *   <li>{@link #uploadLogo}: Original speichern, Status {@code PENDING} – sofort sichtbar, keine Wartezeit.</li>
 *   <li>Nach dem Commit geht nur der Schlüssel per RabbitMQ an den Worker (Claim-Check).</li>
 *   <li>{@link #processLogo}: verkleinern, neu speichern, {@code ACTIVE}, Original löschen.</li>
 * </ol>
 */
@Service
public class CompanyLogoService {

    private static final Logger log = LoggerFactory.getLogger(CompanyLogoService.class);

    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorageService;
    private final LogoScaler logoScaler;
    private final ImageEventPublisher imageEventPublisher;
    private final CompanyEventPublisher companyEventPublisher;

    public CompanyLogoService(CompanyRepository companyRepository,
                              FileStorageService fileStorageService,
                              LogoScaler logoScaler,
                              ImageEventPublisher imageEventPublisher,
                              CompanyEventPublisher companyEventPublisher) {
        this.companyRepository = companyRepository;
        this.fileStorageService = fileStorageService;
        this.logoScaler = logoScaler;
        this.imageEventPublisher = imageEventPublisher;
        this.companyEventPublisher = companyEventPublisher;
    }

    @Transactional
    public void uploadLogo(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Es wurde keine gültige Datei übergeben.");
        }
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Betrieb für diesen Benutzer nicht gefunden: " + email));

        String previousKey = company.getProfileImageKey();
        String originalKey = fileStorageService.uploadFile(file);
        company.setProfileImageKey(originalKey);
        company.setLogoStatus(LogoStatus.PENDING);
        companyRepository.save(company);

        Long companyId = company.getId();
        AfterCommit.run(() -> {
            if (previousKey != null && !previousKey.isBlank()) {
                fileStorageService.deleteFile(previousKey);
            }
            imageEventPublisher.publishImageUploadedEvent(companyId, originalKey);
        });
        publishIfOnline(company);
    }

    /**
     * Vom Worker aufgerufen. Idempotent: Gehört der Schlüssel nicht mehr zum Betrieb (neueres Logo,
     * doppelte Zustellung), passiert nichts. Vorübergehende Fehler (Speicher, DB) werden geworfen,
     * damit RabbitMQ wiederholt und zuletzt in die Dead-Letter-Queue legt.
     */
    @Transactional
    public void processLogo(Long companyId, String originalKey) {
        Optional<Company> current = currentLogoOwner(companyId, originalKey);
        if (current.isEmpty()) {
            log.info("Logo {} von Betrieb {} ist nicht mehr aktuell – übersprungen", originalKey, companyId);
            return;
        }
        Company company = current.get();

        Optional<StoredFile> original = fileStorageService.load(originalKey);
        if (original.isEmpty()) {
            markFailed(company, "Original fehlt im Speicher");
            return;
        }

        StoredFile scaled;
        try {
            scaled = logoScaler.scale(original.get());
        } catch (LogoNotReadableException e) {
            markFailed(company, e.getMessage());
            return;
        }

        String scaledKey = fileStorageService.store(scaled.content(), scaled.contentType());
        company.setProfileImageKey(scaledKey);
        company.setLogoStatus(LogoStatus.ACTIVE);
        companyRepository.save(company);

        AfterCommit.run(() -> fileStorageService.deleteFile(originalKey));
        publishIfOnline(company);
        log.info("Logo von Betrieb {} verkleinert: {} -> {}", companyId, originalKey, scaledKey);
    }

    /** Aus der Dead-Letter-Queue: alle Versuche gescheitert. */
    @Transactional
    public void markLogoFailed(Long companyId, String originalKey) {
        currentLogoOwner(companyId, originalKey)
                .ifPresent(company -> markFailed(company, "Verarbeitung nach mehreren Versuchen gescheitert"));
    }

    private Optional<Company> currentLogoOwner(Long companyId, String logoKey) {
        return companyRepository.findById(companyId)
                .filter(company -> logoKey != null && logoKey.equals(company.getProfileImageKey()));
    }

    private void markFailed(Company company, String reason) {
        log.warn("Logo von Betrieb {} fehlgeschlagen: {}", company.getId(), reason);
        company.setLogoStatus(LogoStatus.FAILED);
        companyRepository.save(company);
    }

    private void publishIfOnline(Company company) {
        if (company.isOnline()) {
            companyEventPublisher.publishUpdate(company);
        }
    }
}
