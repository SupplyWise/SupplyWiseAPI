package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.config.CustomAuthenticationDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthHandler {

    public String getAuthenticatedCognitoSub() {
        // Extract Cognito sub from custom authentication details
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getAuthenticatedAccessToken() {
        // Extract access token from credentials
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    public List<String> getAuthenticatedUserRoles() {
        // Extract roles from authorities
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    public String getAuthenticatedCompanyId() {
        // Extract companyId from custom authentication details
        CustomAuthenticationDetails details = getCustomAuthenticationDetails();
        return details != null ? details.getCompanyId() : null;
    }

    public String getAuthenticatedRestaurantId() {
        // Extract restaurantId from custom authentication details
        CustomAuthenticationDetails details = getCustomAuthenticationDetails();
        return details != null ? details.getRestaurantId() : null;
    }

    // Temporary function to avoid compilation errors
    public User getAuthenticatedUser() {
        return null;
    }

    private CustomAuthenticationDetails getCustomAuthenticationDetails() {
        // Safely extract custom details from SecurityContext
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof CustomAuthenticationDetails customAuthenticationDetails) {
            return customAuthenticationDetails;
        }
        return null;
    }
}
