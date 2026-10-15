package com.baumexpert.brennholzdemo.discovery.infrastructure.messaging;

import com.baumexpert.brennholzdemo.discovery.application.eventhandler.CompanyProfileUpdatedEventHandler;
import com.baumexpert.brennholzdemo.discovery.application.eventhandler.CompanyPublishedEventHandler;
import com.baumexpert.brennholzdemo.discovery.application.eventhandler.CompanyUnpublishedEventHandler;
import com.baumexpert.brennholzdemo.discovery.application.inbound.CompanyProfileUpdatedEvent;
import com.baumexpert.brennholzdemo.discovery.application.inbound.CompanyPublishedEvent; // FIX: Fehlender Import
import com.baumexpert.brennholzdemo.discovery.application.inbound.CompanyUnpublishedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqDiscoveryConsumer {

    private final CompanyPublishedEventHandler publishedEventHandler;
    private final CompanyUnpublishedEventHandler unpublishedEventHandler;
    private final CompanyProfileUpdatedEventHandler updatedEventHandler;

    public RabbitMqDiscoveryConsumer(CompanyPublishedEventHandler publishedEventHandler,
                                     CompanyUnpublishedEventHandler unpublishedEventHandler,
                                     CompanyProfileUpdatedEventHandler updatedEventHandler) {
        this.publishedEventHandler = publishedEventHandler;
        this.unpublishedEventHandler = unpublishedEventHandler;
        this.updatedEventHandler = updatedEventHandler;
    }

    // Lauscht auf neue Veröffentlichungen
    @RabbitListener(queues = "discovery.company.published.queue")
    public void handleCompanyPublished(CompanyPublishedEvent event) {
        publishedEventHandler.handle(event);
    }

    // Lauscht auf Offline-Nahmen
    @RabbitListener(queues = "discovery.company.unpublished.queue")
    public void handleCompanyUnpublished(CompanyUnpublishedEvent event) {
        unpublishedEventHandler.handle(event);
    }

    // Lauscht auf fortlaufende Updates bereits veröffentlichter Profile
    @RabbitListener(queues = "discovery.company.updated.queue")
    public void handleCompanyUpdated(CompanyProfileUpdatedEvent event) {
        updatedEventHandler.handle(event);
    }
}