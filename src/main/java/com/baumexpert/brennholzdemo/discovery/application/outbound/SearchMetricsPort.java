package com.baumexpert.brennholzdemo.discovery.application.outbound;

import com.baumexpert.brennholzdemo.discovery.domain.valueobject.SearchIntent;

/**
 * Outbound-Port (Interface) für die Erfassung von Such- und Interaktions-Metriken im Discovery-Modul.
 */
public interface SearchMetricsPort {

    /**
     * Erfasst, dass eine spezifische Suche durchgeführt wurde.
     *
     * @param intent Die Suchabsicht des Nutzers (Service und Ort/PLZ).
     */
    void incrementSearchCount(SearchIntent intent);
}