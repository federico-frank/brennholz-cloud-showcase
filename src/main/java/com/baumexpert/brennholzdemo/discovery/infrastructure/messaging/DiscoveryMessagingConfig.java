package com.baumexpert.brennholzdemo.discovery.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscoveryMessagingConfig {

    // WICHTIG: Muss exakt mit dem Sender übereinstimmen
    public static final String EXCHANGE_NAME = "company.discovery.exchange";

    // Queue 1: Profil online geschaltet
    public static final String PUBLISHED_QUEUE = "discovery.company.published.queue";
    public static final String PUBLISHED_ROUTING_KEY = "company.published";

    // Queue 2: Profil offline geschaltet
    public static final String UNPUBLISHED_QUEUE = "discovery.company.unpublished.queue";
    public static final String UNPUBLISHED_ROUTING_KEY = "company.unpublished";

    // Queue 3: Profil aktualisiert (inklusive Geodaten, Tags etc.)
    public static final String UPDATED_QUEUE = "discovery.company.updated.queue";
    public static final String UPDATED_ROUTING_KEY = "company.updated";

    @Bean
    public TopicExchange companyExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // --- Setup für Published Event ---
    @Bean
    public Queue companyPublishedQueue() {
        return new Queue(PUBLISHED_QUEUE, true);
    }

    @Bean
    public Binding bindingPublished(Queue companyPublishedQueue, TopicExchange companyExchange) {
        return BindingBuilder
                .bind(companyPublishedQueue)
                .to(companyExchange)
                .with(PUBLISHED_ROUTING_KEY);
    }

    // --- Setup für Unpublished Event ---
    @Bean
    public Queue companyUnpublishedQueue() {
        return new Queue(UNPUBLISHED_QUEUE, true);
    }

    @Bean
    public Binding bindingUnpublished(Queue companyUnpublishedQueue, TopicExchange companyExchange) {
        return BindingBuilder
                .bind(companyUnpublishedQueue)
                .to(companyExchange)
                .with(UNPUBLISHED_ROUTING_KEY);
    }

    // --- Setup für Updated Event ---
    @Bean
    public Queue companyUpdatedQueue() {
        return new Queue(UPDATED_QUEUE, true);
    }

    @Bean
    public Binding bindingUpdated(Queue companyUpdatedQueue, TopicExchange companyExchange) {
        return BindingBuilder
                .bind(companyUpdatedQueue)
                .to(companyExchange)
                .with(UPDATED_ROUTING_KEY);
    }

}