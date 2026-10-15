package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.image;

import com.baumexpert.brennholzdemo.company.profile.application.CompanyLogoService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Inbound-Adapter: nimmt Logo-Nachrichten aus RabbitMQ und ruft den Use Case auf.
 * Misst Dauer und Ergebnis als Prometheus-Metrik {@code logo_processing_seconds{outcome}}.
 */
@Component
public class RabbitMQImageWorker {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQImageWorker.class);

    private final CompanyLogoService logoService;
    private final MeterRegistry meterRegistry;

    public RabbitMQImageWorker(CompanyLogoService logoService, MeterRegistry meterRegistry) {
        this.logoService = logoService;
        this.meterRegistry = meterRegistry;
    }

    @RabbitListener(queues = ImageMessagingConfig.LOGO_QUEUE, containerFactory = ImageMessagingConfig.LOGO_LISTENER_FACTORY)
    public void process(LogoUploadedMessage message) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "error";
        try {
            logoService.processLogo(message.companyId(), message.originalKey());
            outcome = "ok";
        } finally {
            sample.stop(Timer.builder("logo.processing")
                    .description("Dauer der asynchronen Logo-Verarbeitung")
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }

    /** Alle Versuche gescheitert: Betrieb sieht „fehlgeschlagen“ und kann neu hochladen. */
    @RabbitListener(queues = ImageMessagingConfig.LOGO_DEAD_LETTER_QUEUE, containerFactory = ImageMessagingConfig.LOGO_LISTENER_FACTORY)
    public void deadLetter(LogoUploadedMessage message) {
        log.error("Logo {} von Betrieb {} in der Dead-Letter-Queue", message.originalKey(), message.companyId());
        Counter.builder("logo.processing.dead.letters").register(meterRegistry).increment();
        logoService.markLogoFailed(message.companyId(), message.originalKey());
    }
}
