package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.discovery;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompanyEventPublisherConfig {

    // WICHTIG: Muss exakt dem EXCHANGE-Namen im RabbitMQCompanyEventAdapter entsprechen
    public static final String EXCHANGE_NAME = "company.discovery.exchange";

    // Routing Keys für die unterschiedlichen Events
    public static final String ROUTING_KEY_PUBLISHED = "company.published";
    public static final String ROUTING_KEY_UNPUBLISHED = "company.unpublished";
    public static final String ROUTING_KEY_UPDATED = "company.updated";

    @Bean
    public TopicExchange publisherCompanyExchange() {
        // Erstellt den Exchange auf dem Broker (durable = true, autoDelete = false)
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}