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

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            System.out.println("Username: " + username);
            System.out.println("User" + userService.getUserByEmail(username));
            return userService.findByEmailUser(username);  // Fetch the User entity by username
        }

        return null;
    }
}
