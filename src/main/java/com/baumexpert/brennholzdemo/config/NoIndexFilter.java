package com.baumexpert.brennholzdemo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Sperrt die gesamte Anwendung für Suchmaschinen (F-043 S4, F-047): Header auf jeder Antwort –
 * Seiten, Bilder, Weiterleitungen, Fehler. Läuft vor Spring Security, damit auch 302 zum Login ihn tragen.
 * In der Cloud setzt Caddy denselben Header zusätzlich (auch für Grafana).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "seo.noindex", havingValue = "true", matchIfMissing = true)
public class NoIndexFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Robots-Tag";
    static final String VALUE = "noindex, nofollow, noarchive";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader(HEADER, VALUE);
        chain.doFilter(request, response);
    }
}
