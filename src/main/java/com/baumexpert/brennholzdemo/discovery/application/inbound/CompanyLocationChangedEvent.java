package com.baumexpert.brennholzdemo.discovery.application.inbound;

/**
 * DTO für das eingehende Event, wenn ein Betrieb im Company-Modul seine Adresse/Koordinaten ändert.
 */
public class CompanyLocationChangedEvent {

    private String companyId;
    private double newLatitude;
    private double newLongitude;

    public CompanyLocationChangedEvent() {
    }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public double getNewLatitude() { return newLatitude; }
    public void setNewLatitude(double newLatitude) { this.newLatitude = newLatitude; }

    public double getNewLongitude() { return newLongitude; }
    public void setNewLongitude(double newLongitude) { this.newLongitude = newLongitude; }
}