package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.services.CompanyService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.CognitoUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping("/api/company")
@Tag(name = "Company Controller", description = "API for Company operations")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CognitoUtils cognitoUtils;

    @Autowired
    private AuthHandler authHandler;

    private final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Operation(summary = "Create a new company", description = "Create a new company with the given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "403", description = "User is not eligible to create a company")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DISASSOCIATED')")
    @PostMapping("/create")
    public ResponseEntity<Object> createCompany(@RequestParam String name) {

        logger.info("Attempting to create a new company");

        String cognitoSub = authHandler.getAuthenticatedCognitoSub();
        
        // Create a new company
        Company company = new Company(name, cognitoSub);
        companyService.createCompany(company);
        logger.info("Company created successfully");

        // Make a request to a Lambda function that will promote the user to FRANCHISE_OWNER, and update the user's company
        // The URL: https://zo9bnne4ec.execute-api.eu-west-1.amazonaws.com/dev/user-management/promote_to_owner
        // Send the company ID in the request body as JSON
        String message = cognitoUtils.promoteDisassociatedToOwner(company);
        if (message != null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @Operation(summary = "Get company details", description = "Get the details of a company by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company details fetched successfully"),
            @ApiResponse(responseCode = "403", description = "User is not eligible to view company details"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN'')")
    @GetMapping("/details/{id}")
    public ResponseEntity<String> getCompanyDetailsById(@PathVariable UUID id) {

        logger.info("Attempting to fetch company details by ID");

        // Check if company exists
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            logger.error("Company not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company ID does not exist.");
        }

        logger.info("Company details fetched successfully");
        return ResponseEntity.ok(company.toString());
    }

    @Operation(summary = "Get company details", description = "Get the details of the company of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company details fetched successfully"),
            @ApiResponse(responseCode = "403", description = "User is not eligible to view company details"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER_MASTER', 'ROLE_MANAGER')")
    @GetMapping("/details")
    public ResponseEntity<Object> getCompanyDetails() {

        logger.info("Attempting to fetch company details");

        // Get the authenticated user's company id
        UUID id = UUID.fromString(authHandler.getAuthenticatedCompanyId());

        // Check if company exists
        Company company = companyService.getCompanyById(id);
        if (company == null) {
            logger.error("Company not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company ID does not exist.");
        }

        logger.info("Company details fetched successfully");
        return ResponseEntity.status(HttpStatus.OK).body(company);
    }
}
