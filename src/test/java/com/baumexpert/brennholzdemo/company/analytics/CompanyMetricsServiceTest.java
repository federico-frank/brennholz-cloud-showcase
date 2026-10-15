package com.baumexpert.brennholzdemo.company.analytics;

import com.baumexpert.brennholzdemo.company.CompanyRepository;
import com.baumexpert.brennholzdemo.company.event.CompanyRegisteredEvent;
import com.baumexpert.brennholzdemo.company.event.ProfileViewedEvent;
import com.baumexpert.brennholzdemo.discovery.event.SearchImpressionEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * F-041 S3: Zähler existieren ab 0, damit increase(...[$__range]) auch das erste Ereignis zählt
 * und Grafana 0 statt „No data“ zeigt. Ein Zähler je Betrieb, ohne Suchbegriff-Label (Q6).
 */
class CompanyMetricsServiceTest {

    private SimpleMeterRegistry registry;
    private CompanyRepository companyRepository;
    private CompanyMetricsService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        companyRepository = mock(CompanyRepository.class);
        service = new CompanyMetricsService(registry, companyRepository);
    }

    private double zaehler(String name, String companyId) {
        var counter = registry.find(name).tag("company_id", companyId).counter();
        return counter == null ? -1 : counter.count();
    }

    @Test
    void beimStartBekommtJederBetriebZaehlerMitNull() {
        when(companyRepository.findAllIds()).thenReturn(List.of(1L, 2L));

        service.registerCountersForAllCompanies();

        assertThat(zaehler("company.profile.impressions", "1")).isZero();
        assertThat(zaehler("company.profile.views", "2")).isZero();
    }

    @Test
    void neuRegistrierterBetriebBekommtZaehlerMitNull() {
        service.onCompanyRegistered(new CompanyRegisteredEvent(7L));

        assertThat(zaehler("company.profile.impressions", "7")).isZero();
        assertThat(zaehler("company.profile.views", "7")).isZero();
    }

    @Test
    void ereignisseZaehlenJeBetriebOhneZusatzLabels() {
        service.onSearchImpression(new SearchImpressionEvent(List.of(1L, 2L), "brennholz"));
        service.onSearchImpression(new SearchImpressionEvent(List.of(1L), "baumpflege"));
        service.onProfileView(new ProfileViewedEvent(1L, "direct"));

        assertThat(zaehler("company.profile.impressions", "1")).isEqualTo(2);
        assertThat(zaehler("company.profile.impressions", "2")).isEqualTo(1);
        assertThat(zaehler("company.profile.views", "1")).isEqualTo(1);
        assertThat(registry.find("company.profile.impressions").counters()).hasSize(2);
        assertThat(registry.find("company.profile.impressions").counter().getId().getTags()).hasSize(1);
    }
}
