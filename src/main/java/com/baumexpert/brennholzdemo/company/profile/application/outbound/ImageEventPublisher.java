package com.baumexpert.brennholzdemo.company.profile.application.outbound;

public interface ImageEventPublisher {
    void publishImageUploadedEvent(Long companyId, String s3Key);
}