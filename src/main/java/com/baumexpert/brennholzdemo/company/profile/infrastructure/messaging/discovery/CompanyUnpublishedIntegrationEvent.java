package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.discovery;

public class CompanyUnpublishedIntegrationEvent {
    private String companyId;

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}
