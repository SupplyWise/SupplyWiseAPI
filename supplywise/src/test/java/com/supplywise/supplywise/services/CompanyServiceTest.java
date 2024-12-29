package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.repositories.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCompany_ShouldSaveCompany() {
        // Company data
        Company company = new Company();
        company.setName("Test Company");

        // Mock the repository to return the company when saved
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        // Execute the method
        Company createdCompany = companyService.createCompany(company);

        // Verify that the company is saved
        verify(companyRepository, times(1)).save(company);

        // Check if the company returned matches the mock
        assertEquals("Test Company", createdCompany.getName());
    }

    @Test
    void testCompanyExists_CompanyExists_ShouldReturnTrue() {
        // Generate a random UUID for the company
        UUID companyId = UUID.randomUUID();

        // Mock the repository to return true when checking if company exists
        when(companyRepository.existsById(companyId)).thenReturn(true);

        // Execute the method
        boolean exists = companyService.companyExists(companyId);

        // Check if the method returns true
        assertTrue(exists);

        // Verify that the existsById method was called
        verify(companyRepository, times(1)).existsById(companyId);
    }

    @Test
    void testCompanyExists_CompanyDoesNotExist_ShouldReturnFalse() {
        // Generate a random UUID for the company
        UUID companyId = UUID.randomUUID();

        // Mock the repository to return false when checking if company exists
        when(companyRepository.existsById(companyId)).thenReturn(false);

        // Execute the method
        boolean exists = companyService.companyExists(companyId);

        // Check if the method returns false
        assertFalse(exists);

        // Verify that the existsById method was called
        verify(companyRepository, times(1)).existsById(companyId);
    }
}
