package com.baumexpert.brennholzdemo.company.profile.infrastructure.messaging.discovery;

import com.baumexpert.brennholzdemo.company.profile.domain.FulfillmentType;
import java.util.List;
import java.util.Set;

public class CompanyProfileUpdatedIntegrationEvent {
    private String companyId;
    private String companyName;
    private String logoUrl;
    private List<String> tags;

    // --- Kontakt- & Profilfelder ---
    private String description;
    private String email;
    private String phone;
    private String websiteUrl;

    // --- GEODATEN, STANDORT & RADIUS (Read-Model Synchronisation) ---
    private String locationZip;
    private String locationCity;
    private Integer catchmentRadius;
    private double latitude;
    private double longitude;

    private int initialScore;
    private Set<FulfillmentType> fulfillmentTypes;

    // --- Impressum ---
    private String imprintMode;
    private String ceoName;
    private String street;
    private String zipCity;
    private String vatId;
    private String register;
    private String imprintCustomText;

    // --- Getter & Setter ---
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getLocationZip() { return locationZip; }
    public void setLocationZip(String locationZip) { this.locationZip = locationZip; }

    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String locationCity) { this.locationCity = locationCity; }

    public Integer getCatchmentRadius() { return catchmentRadius; }
    public void setCatchmentRadius(Integer catchmentRadius) { this.catchmentRadius = catchmentRadius; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getInitialScore() { return initialScore; }
    public void setInitialScore(int initialScore) { this.initialScore = initialScore; }

    public Set<FulfillmentType> getFulfillmentTypes() { return fulfillmentTypes; }
    public void setFulfillmentTypes(Set<FulfillmentType> fulfillmentTypes) { this.fulfillmentTypes = fulfillmentTypes; }

    public String getImprintMode() { return imprintMode; }
    public void setImprintMode(String imprintMode) { this.imprintMode = imprintMode; }

    public String getCeoName() { return ceoName; }
    public void setCeoName(String ceoName) { this.ceoName = ceoName; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getZipCity() { return zipCity; }
    public void setZipCity(String zipCity) { this.zipCity = zipCity; }

    public String getVatId() { return vatId; }
    public void setVatId(String vatId) { this.vatId = vatId; }

    public String getRegister() { return register; }
    public void setRegister(String register) { this.register = register; }

    public String getImprintCustomText() { return imprintCustomText; }
    public void setImprintCustomText(String imprintCustomText) { this.imprintCustomText = imprintCustomText; }
}