package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.image;

import com.baumexpert.brennholzdemo.company.profile.application.outbound.ImageEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQImageEventPublisherImpl implements ImageEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQImageEventPublisherImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishImageUploadedEvent(Long companyId, String s3Key) {
        // Nur Metadaten – die Bilddaten liegen AES-verschlüsselt im Speicher (Claim-Check)
        rabbitTemplate.convertAndSend(
                ImageMessagingConfig.LOGO_EXCHANGE,
                ImageMessagingConfig.LOGO_ROUTING_KEY,
                new LogoUploadedMessage(companyId, s3Key)
        );
    }
}
