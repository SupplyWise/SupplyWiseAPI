package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthHandler {

    public String getAuthenticatedUsername() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<String> getAuthenticatedUserRoles() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    // Temp function to avoid compilation errors
    public User getAuthenticatedUser() {
        return null;
    }
}
