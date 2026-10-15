package com.baumexpert.brennholzdemo.discovery.event;

import java.util.List;

public record SearchImpressionEvent(List<Long> companyIds, String searchTerm) {
}