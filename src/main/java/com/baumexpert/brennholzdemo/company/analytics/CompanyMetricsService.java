package com.baumexpert.brennholzdemo.company.analytics;

import com.baumexpert.brennholzdemo.company.CompanyRepository;
import com.baumexpert.brennholzdemo.company.event.CompanyRegisteredEvent;
import com.baumexpert.brennholzdemo.company.event.ProfileViewedEvent;
import com.baumexpert.brennholzdemo.discovery.event.SearchImpressionEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Zählt Impressionen und Profilaufrufe je Betrieb für Prometheus/Grafana (F-041).
 * Micrometer macht aus „company.profile.impressions“ die Zeitreihe „company_profile_impressions_total“.
 * Seit F-041 S3: Zähler werden mit 0 angelegt (Start, Registrierung), damit increase(...[$__range])
 * auch das erste Ereignis zählt und Grafana 0 statt „No data“ zeigt; nur das Label company_id (Q6).
 */
@Service
public class CompanyMetricsService {

    static final String IMPRESSIONS = "company.profile.impressions";
    static final String VIEWS = "company.profile.views";

    private final MeterRegistry meterRegistry;
    private final CompanyRepository companyRepository;

    public CompanyMetricsService(MeterRegistry meterRegistry, CompanyRepository companyRepository) {
        this.meterRegistry = meterRegistry;
        this.companyRepository = companyRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerCountersForAllCompanies() {
        companyRepository.findAllIds().forEach(this::registerCounters);
    }

    @EventListener
    public void onCompanyRegistered(CompanyRegisteredEvent event) {
        registerCounters(event.companyId());
    }

    @EventListener
    public void onSearchImpression(SearchImpressionEvent event) {
        if (event.companyIds() == null) return;
        event.companyIds().forEach(companyId -> impressions(companyId).increment());
    }

    @EventListener
    public void onProfileView(ProfileViewedEvent event) {
        views(event.companyId()).increment();
    }

    private void registerCounters(Long companyId) {
        impressions(companyId);
        views(companyId);
    }

    private Counter impressions(Long companyId) {
        return meterRegistry.counter(IMPRESSIONS, "company_id", String.valueOf(companyId));
    }

    private Counter views(Long companyId) {
        return meterRegistry.counter(VIEWS, "company_id", String.valueOf(companyId));
    }
}
