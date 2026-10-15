package com.baumexpert.brennholzdemo.discovery.infrastructure.metrics;

import com.baumexpert.brennholzdemo.discovery.application.outbound.SearchMetricsPort;
import com.baumexpert.brennholzdemo.discovery.domain.valueobject.SearchIntent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PrometheusSearchMetricsAdapter implements SearchMetricsPort {

    private final MeterRegistry meterRegistry;

    public PrometheusSearchMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementSearchCount(SearchIntent intent) {
        Counter.builder("discovery.searches")
                .description("Anzahl der durchgeführten Suchen")
                .tag("service", intent.getService())
                .tag("search_type", intent.getSearchType().name())
                .register(meterRegistry)
                .increment();
    }

    // incrementProfileImpression und incrementProfileClick wurden hier entfernt,
    // um den Micrometer-Konflikt mit dem CompanyMetricsService zu beheben.
}