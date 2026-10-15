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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * F-043 S7: Logo sofort sichtbar, danach asynchron verkleinert – idempotent und mit sauberem Fehlerstatus.
 */
class CompanyLogoServiceTest {

    private static final String EMAIL = "betrieb@test.de";
    private static final StoredFile ORIGINAL = new StoredFile(new byte[]{1, 2, 3}, "image/jpeg");
    private static final StoredFile SCALED = new StoredFile(new byte[]{9}, "image/jpeg");

    private final CompanyRepository repository = mock(CompanyRepository.class);
    private final FileStorageService storage = mock(FileStorageService.class);
    private final LogoScaler scaler = mock(LogoScaler.class);
    private final ImageEventPublisher imageEvents = mock(ImageEventPublisher.class);
    private final CompanyEventPublisher companyEvents = mock(CompanyEventPublisher.class);
    private final CompanyLogoService service = new CompanyLogoService(repository, storage, scaler, imageEvents, companyEvents);

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(7L);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(company));
        when(repository.findById(7L)).thenReturn(Optional.of(company));
    }

    @Test
    void uploadZeigtOriginalSofortUndStoesstVerarbeitungAn() {
        company.setProfileImageKey("alt.jpg");
        company.setOnline(true);
        when(storage.uploadFile(any())).thenReturn("original.jpg");

        service.uploadLogo(EMAIL, new MockMultipartFile("file", "logo.jpg", "image/jpeg", new byte[]{1}));

        assertThat(company.getProfileImageKey()).isEqualTo("original.jpg");
        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.PENDING);
        verify(repository).save(company);
        verify(storage).deleteFile("alt.jpg");
        verify(imageEvents).publishImageUploadedEvent(7L, "original.jpg");
        verify(companyEvents).publishUpdate(company);
    }

    @Test
    void verarbeitungErsetztOriginalDurchVerkleinertesLogo() {
        company.setProfileImageKey("original.jpg");
        company.setLogoStatus(LogoStatus.PENDING);
        company.setOnline(true);
        when(storage.load("original.jpg")).thenReturn(Optional.of(ORIGINAL));
        when(scaler.scale(ORIGINAL)).thenReturn(SCALED);
        when(storage.store(SCALED.content(), "image/jpeg")).thenReturn("klein.jpg");

        service.processLogo(7L, "original.jpg");

        assertThat(company.getProfileImageKey()).isEqualTo("klein.jpg");
        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.ACTIVE);
        verify(storage).deleteFile("original.jpg");
        verify(companyEvents).publishUpdate(company);
    }

    @Test
    void veralteteOderDoppelteNachrichtAendertNichts() {
        company.setProfileImageKey("neueres-logo.jpg");

        service.processLogo(7L, "original.jpg");

        verifyNoInteractions(storage, scaler, companyEvents);
        verify(repository, never()).save(any());
    }

    @Test
    void unlesbaresBildEndetOhneWiederholungMitFailed() {
        company.setProfileImageKey("original.jpg");
        when(storage.load("original.jpg")).thenReturn(Optional.of(ORIGINAL));
        when(scaler.scale(ORIGINAL)).thenThrow(new LogoNotReadableException("kaputt"));

        service.processLogo(7L, "original.jpg");

        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.FAILED);
        verify(storage, never()).store(any(), any());
    }

    @Test
    void fehlendesOriginalEndetMitFailed() {
        company.setProfileImageKey("original.jpg");
        when(storage.load("original.jpg")).thenReturn(Optional.empty());

        service.processLogo(7L, "original.jpg");

        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.FAILED);
    }

    @Test
    void deadLetterMarkiertNurDasAktuelleLogoAlsFailed() {
        company.setProfileImageKey("neueres-logo.jpg");
        company.setLogoStatus(LogoStatus.PENDING);

        service.markLogoFailed(7L, "original.jpg");
        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.PENDING);

        service.markLogoFailed(7L, "neueres-logo.jpg");
        assertThat(company.getLogoStatus()).isEqualTo(LogoStatus.FAILED);
    }
}
