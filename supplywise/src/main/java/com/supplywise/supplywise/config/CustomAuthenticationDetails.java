package com.supplywise.supplywise.config;

import java.util.List;

public class CustomAuthenticationDetails {
    private final String username;
    private final String companyId;   // Optional
    private final String restaurantId; // Optional
    private final List<String> roles;

    public CustomAuthenticationDetails(String username, String companyId, String restaurantId, List<String> roles) {
        this.username = username;
        this.companyId = companyId;
        this.restaurantId = restaurantId;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<String> getRoles() {
        return roles;
    }
}