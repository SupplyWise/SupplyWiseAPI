package com.supplywise.supplywise.services;

import com.supplywise.supplywise.config.CustomAuthenticationDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

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

    public boolean hasRole(String role) {
        // Check if user has a specific role
        return getAuthenticatedUserRoles().contains(role);
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

    public String getAuthenticatedUsername() {
        // Extract username from custom authentication details
        CustomAuthenticationDetails details = getCustomAuthenticationDetails();
        return details != null ? details.getUsername() : null;
    }

    private CustomAuthenticationDetails getCustomAuthenticationDetails() {
        // Safely extract custom details from SecurityContext
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        logger.info("Details: {}", details);
        
        if (details instanceof CustomAuthenticationDetails customAuthenticationDetails) {
            return customAuthenticationDetails;
        }
        return null;
    }
}
