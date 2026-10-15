package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.image;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie der Logo-Verarbeitung (F-043 S7):
 * <pre>
 * company.logo.exchange --logo.uploaded--> company.logo.processing
 *     └─(3 Versuche gescheitert)─> company.logo.dlx --> company.logo.processing.dlq
 * </pre>
 */
@Configuration
public class ImageMessagingConfig {

    public static final String LOGO_EXCHANGE = "company.logo.exchange";
    public static final String LOGO_QUEUE = "company.logo.processing";
    public static final String LOGO_ROUTING_KEY = "logo.uploaded";

    public static final String LOGO_DEAD_LETTER_EXCHANGE = "company.logo.dlx";
    public static final String LOGO_DEAD_LETTER_QUEUE = "company.logo.processing.dlq";

    public static final String LOGO_LISTENER_FACTORY = "logoListenerContainerFactory";

    @Bean
    public DirectExchange logoExchange() {
        return new DirectExchange(LOGO_EXCHANGE);
    }

    @Bean
    public Queue logoQueue() {
        // durable: überlebt einen Broker-Neustart; abgelehnte Nachrichten gehen an die Dead-Letter-Exchange
        return QueueBuilder.durable(LOGO_QUEUE)
                .deadLetterExchange(LOGO_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(LOGO_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding logoBinding(Queue logoQueue, DirectExchange logoExchange) {
        return BindingBuilder.bind(logoQueue).to(logoExchange).with(LOGO_ROUTING_KEY);
    }

    @Bean
    public DirectExchange logoDeadLetterExchange() {
        return new DirectExchange(LOGO_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue logoDeadLetterQueue() {
        return QueueBuilder.durable(LOGO_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding logoDeadLetterBinding(Queue logoDeadLetterQueue, DirectExchange logoDeadLetterExchange) {
        return BindingBuilder.bind(logoDeadLetterQueue).to(logoDeadLetterExchange).with(LOGO_ROUTING_KEY);
    }

    /**
     * Eigene Factory nur für Logos: 3 Versuche mit Backoff (1 s, 2 s), danach ablehnen ohne Requeue –
     * RabbitMQ legt die Nachricht dann in die Dead-Letter-Queue. Andere Listener bleiben unverändert.
     */
    @Bean(LOGO_LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory logoListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                             MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxRetries(2)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }
}
