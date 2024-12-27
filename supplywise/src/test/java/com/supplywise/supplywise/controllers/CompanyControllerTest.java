package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.config.JwtAuthenticationFilter;
import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.CompanyService;
import com.supplywise.supplywise.services.CognitoUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private AuthHandler authHandler;

    @MockBean
    private CognitoUtils cognitoUtils;

    @InjectMocks
    private CompanyController companyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void testCreateCompany_Success() throws Exception {

        // Mock the company creation
        String companyName = "TechCorp";
        Company company = new Company();
        company.setName(companyName);
        when(companyService.createCompany(any(Company.class))).thenReturn(company);
        when(authHandler.getAuthenticatedCompanyId()).thenReturn("mock-company-id");
        when(cognitoUtils.promoteDisassociatedToOwner(any(Company.class))).thenReturn(null);

        // Perform the request with the mocked JWT token
        mockMvc.perform(post("/api/company/create")
            .param("name", companyName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(companyName));
    }


    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"FRANCHISE_OWNER"})
    void testCreateCompany_IneligibleUser_WithJWT() throws Exception {
        String companyName = "TechCorp";

        // Make the request with the mocked token
        mockMvc.perform(post("/api/company/create")
                .param("name", companyName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetCompanyDetails_UserIsManager_ShouldReturnCompanyDetails() throws Exception {
        // Create mock company data
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Company Name");
        
        // Mock the auth handler to return the companyId (authHandler.getAuthenticatedCompanyId())
        when(authHandler.getAuthenticatedCompanyId()).thenReturn(company.getId().toString());
        when(companyService.getCompanyById(company.getId())).thenReturn(company);
    
        // Make the request with the mock token
        mockMvc.perform(get("/api/company/details")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(company.getId().toString()));
            }
}
