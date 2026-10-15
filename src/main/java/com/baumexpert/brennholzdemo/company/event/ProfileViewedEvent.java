package com.baumexpert.brennholzdemo.company.event;

public record ProfileViewedEvent(Long companyId, String source) {
}