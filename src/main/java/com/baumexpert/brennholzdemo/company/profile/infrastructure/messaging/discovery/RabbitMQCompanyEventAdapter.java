package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.discovery;

import com.baumexpert.brennholzdemo.company.Company;
import com.baumexpert.brennholzdemo.company.profile.application.outbound.CompanyEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQCompanyEventAdapter implements CompanyEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = CompanyEventPublisherConfig.EXCHANGE_NAME;

    public RabbitMQCompanyEventAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOnline(Company company) {
        CompanyPublishedIntegrationEvent event = new CompanyPublishedIntegrationEvent();
        event.setCompanyId(String.valueOf(company.getId()));
        event.setCompanyName(company.getCompanyName());
        event.setLogoUrl(company.getProfileImageKey());
        event.setTags(company.getTags());

        event.setDescription(company.getDescription());

        // DATENSCHUTZ FILTER: Nur übermitteln, wenn der Toggle aktiviert ist
        event.setEmail(company.isShowContactEmail() ? company.getContactEmail() : null);
        event.setPhone(company.isShowPhone() ? company.getPhone() : null);
        event.setWebsiteUrl(company.isShowWebsite() ? company.getWebsite() : null);

        event.setLocationZip(company.getLocationZip());
        event.setLocationCity(company.getLocationCity());
        event.setCatchmentRadius(company.getCatchmentRadius());

        event.setLatitude(company.getLatitude() != null ? company.getLatitude() : 0.0);
        event.setLongitude(company.getLongitude() != null ? company.getLongitude() : 0.0);
        event.setFulfillmentTypes(company.getFulfillmentTypes());
        event.setInitialScore(company.calculateInitialScore());

        event.setImprintMode(company.getImprintMode());
        event.setCeoName(company.getImprintCeoName());
        event.setStreet(company.getImprintStreet());
        event.setZipCity(company.getImprintZipCity());
        event.setVatId(company.getImprintVatId());
        event.setRegister(company.getImprintRegister());
        event.setImprintCustomText(company.getImprintCustomText());

        rabbitTemplate.convertAndSend(EXCHANGE, CompanyEventPublisherConfig.ROUTING_KEY_PUBLISHED, event);
    }

    @Override
    public void publishOffline(Long companyId) {
        CompanyUnpublishedIntegrationEvent event = new CompanyUnpublishedIntegrationEvent();
        event.setCompanyId(String.valueOf(companyId));

        rabbitTemplate.convertAndSend(EXCHANGE, CompanyEventPublisherConfig.ROUTING_KEY_UNPUBLISHED, event);
    }

    @Override
    public void publishUpdate(Company company) {
        CompanyProfileUpdatedIntegrationEvent event = new CompanyProfileUpdatedIntegrationEvent();
        event.setCompanyId(String.valueOf(company.getId()));
        event.setCompanyName(company.getCompanyName());
        event.setLogoUrl(company.getProfileImageKey());
        event.setTags(company.getTags());

        event.setDescription(company.getDescription());

        // DATENSCHUTZ FILTER: Nur übermitteln, wenn der Toggle aktiviert ist
        event.setEmail(company.isShowContactEmail() ? company.getContactEmail() : null);
        event.setPhone(company.isShowPhone() ? company.getPhone() : null);
        event.setWebsiteUrl(company.isShowWebsite() ? company.getWebsite() : null);

        event.setLocationZip(company.getLocationZip());
        event.setLocationCity(company.getLocationCity());
        event.setCatchmentRadius(company.getCatchmentRadius());

        event.setLatitude(company.getLatitude() != null ? company.getLatitude() : 0.0);
        event.setLongitude(company.getLongitude() != null ? company.getLongitude() : 0.0);
        event.setFulfillmentTypes(company.getFulfillmentTypes());
        event.setInitialScore(company.calculateInitialScore());

        event.setImprintMode(company.getImprintMode());
        event.setCeoName(company.getImprintCeoName());
        event.setStreet(company.getImprintStreet());
        event.setZipCity(company.getImprintZipCity());
        event.setVatId(company.getImprintVatId());
        event.setRegister(company.getImprintRegister());
        event.setImprintCustomText(company.getImprintCustomText());

        rabbitTemplate.convertAndSend(EXCHANGE, CompanyEventPublisherConfig.ROUTING_KEY_UPDATED, event);
    }
}