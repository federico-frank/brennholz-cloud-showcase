package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.discovery;

/**
 * DTO für den Versand von Standortänderungen via RabbitMQ.
 */
public record CompanyLocationChangedIntegrationEvent(
        String companyId,
        double newLatitude,
        double newLongitude
) {}