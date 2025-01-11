package com.supplywise.supplywise.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

class CustomAuthenticationDetailsTest {

    private CustomAuthenticationDetails customAuthenticationDetails;
    private String username;
    private String companyId;
    private String restaurantId;
    private List<String> roles;

    @BeforeEach
    void setUp() {
        // Arrange
        username = "testUser";
        companyId = "12345";
        restaurantId = "67890";
        roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        
        // Create an instance of CustomAuthenticationDetails
        customAuthenticationDetails = new CustomAuthenticationDetails(username, companyId, restaurantId, roles);
    }

    @Test
    void testGetUsername() {
        // Act & Assert
        assertEquals(username, customAuthenticationDetails.getUsername());
    }

    @Test
    void testGetCompanyId() {
        // Act & Assert
        assertEquals(companyId, customAuthenticationDetails.getCompanyId());
    }

    @Test
    void testGetRestaurantId() {
        // Act & Assert
        assertEquals(restaurantId, customAuthenticationDetails.getRestaurantId());
    }

    @Test
    void testGetRoles() {
        // Act & Assert
        assertEquals(roles, customAuthenticationDetails.getRoles());
    }

    @Test
    void testConstructorNullOptionalFields() {
        // Arrange: Pass null for optional fields
        customAuthenticationDetails = new CustomAuthenticationDetails(username, null, null, roles);

        // Act & Assert
        assertNull(customAuthenticationDetails.getCompanyId());
        assertNull(customAuthenticationDetails.getRestaurantId());
    }

    @Test
    void testConstructorEmptyRoles() {
        // Arrange: Pass an empty list for roles
        List<String> emptyRoles = Arrays.asList();
        customAuthenticationDetails = new CustomAuthenticationDetails(username, companyId, restaurantId, emptyRoles);

        // Act & Assert
        assertTrue(customAuthenticationDetails.getRoles().isEmpty());
    }
}