package com.baumexpert.brennholzdemo.company.profile.application.outbound;

import com.baumexpert.brennholzdemo.company.Company;

public interface CompanyEventPublisher {
    void publishOnline(Company company);
    void publishOffline(Long companyId);
    void publishUpdate(Company company);
}