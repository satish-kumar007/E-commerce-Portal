package com.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    @Column(nullable = false, length = 255)
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    @Column(length = 255)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String country;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Invalid postal code format")
    @Column(nullable = false, length = 20)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean isActive = true;

    public enum AddressType {
        PERMANENT, SHIPPING, BILLING
    }

    public Address() {
    }

    public Address(String addressLine1, String addressLine2, String city, String state, 
                   String country, String postalCode, AddressType addressType) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.addressType = addressType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            fullAddress.append(", ").append(addressLine2);
        }
        fullAddress.append(", ").append(city);
        fullAddress.append(", ").append(state);
        fullAddress.append(", ").append(country);
        fullAddress.append(" - ").append(postalCode);
        return fullAddress.toString();
    }
}
