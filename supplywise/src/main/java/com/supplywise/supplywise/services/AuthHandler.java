package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthHandler {

    private final UserService userService;

    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    // Method to retrieve the authenticated user
    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null || !(principal instanceof UserDetails)) {
            return null;
        }

        return userService.findByEmailUser(((UserDetails) principal).getUsername());
    }
}
