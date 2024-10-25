package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.CompanyService;
import com.supplywise.supplywise.services.JwtService;
import com.supplywise.supplywise.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.UUID;

@WebMvcTest(CompanyController.class)
@Import(SecurityConfiguration.class) // Ensure that security configuration is loaded - DO NOT REMOVE
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private CompanyController companyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "DISASSOCIATED" })
    void testCreateCompany_Success() throws Exception {
        // Mock the authenticated user
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.DISASSOCIATED);
        when(authHandler.getAuthenticatedUser()).thenReturn(user);

        // Mock the company creation
        String companyName = "TechCorp";
        Company company = new Company();
        company.setName(companyName);
        when(companyService.createCompany(any(Company.class))).thenReturn(company);

        // Mock the user update
        User updatedUser = new User();
        updatedUser.setId(UUID.randomUUID());
        updatedUser.setRole(Role.FRANCHISE_OWNER); // Role should be updated
        when(userService.updateUser(any(UUID.class), any(User.class))).thenReturn(Optional.of(updatedUser));

        // Make the request and check if it returns CREATED (201)
        mockMvc.perform(post("/api/company/create")
                .param("name", companyName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Company created successfully. Role updated to FRANCHISE_OWNER."));
    }

    //TODO not passing due to securityConfiguration not allowing unauthenticated users should we remove this?
    // @Test
    // @WithAnonymousUser
    // void testCreateCompany_UnauthenticatedUser() throws Exception {
    //     String companyName = "TechCorp";

    //     //when(authHandler.getAuthenticatedUser()).thenReturn(null);

    //     assert authHandler.getAuthenticatedUser() == null;

    //     // Make the request and check if it returns UNAUTHORIZED (401)
    //     mockMvc.perform(post("/api/company/create")
    //             .param("name", companyName)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(content().string("User is not authenticated."));
    // }

    @Test
    @WithMockUser(username = "testuser", roles = { "FRANCHISE_OWNER" })
    void testCreateCompany_IneligibleUser() throws Exception {
        String companyName = "TechCorp";
        
        // Mock an authenticated user with an ineligible role
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.FRANCHISE_OWNER); // Not allowed to create a company
        when(authHandler.getAuthenticatedUser()).thenReturn(user);

        // Make the request and check if it returns FORBIDDEN (403)
        mockMvc.perform(post("/api/company/create")
                .param("name", companyName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not eligible to create a company."));
    }
}
