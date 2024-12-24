package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.CompanyService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.UserService;

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
    private AuthHandler authHandler;

    @Autowired
    private UserService userService;

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
        logger.info("Cognito sub: " + cognitoSub);

        // Create a new company
        Company company = new Company(name, cognitoSub);
        companyService.createCompany(company);
        logger.info("Company created successfully");

        // Make a request to a Lambda function that will promote the user to FRANCHISE_OWNER, and update the user's company
        // The URL: https://zo9bnne4ec.execute-api.eu-west-1.amazonaws.com/dev/user-management/promote_to_owner
        // Send the company ID in the request body as JSON
        try {
            String url = "https://zo9bnne4ec.execute-api.eu-west-1.amazonaws.com/dev/user-management/promote_to_owner";
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = String.format("{\"company_id\": \"%s\"}", company.getId().toString());
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authHandler.getAuthenticatedAccessToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Failed to promote user to FRANCHISE_OWNER");
                // Details about the error can be found in the request response
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.body());
            }

            logger.info("User promoted to FRANCHISE_OWNER successfully");
        } catch (Exception e) {
            logger.error("Exception occurred while promoting user to FRANCHISE_OWNER", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to promote user to FRANCHISE_OWNER.");
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
        Company company = userService.getCompanyDetails(id);
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER')")
    @GetMapping("/details")
    public ResponseEntity<Object> getCompanyDetails() {

        logger.info("Attempting to fetch company details");

        // Get the authenticated user's company id
        UUID companyId = UUID.fromString(authHandler.getAuthenticatedCompanyId());

        // Check if company exists
        Company company = userService.getCompanyDetails(companyId);
        if (company == null) {
            logger.error("Company not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company ID does not exist.");
        }

        logger.info("Company details fetched successfully");
        return ResponseEntity.ok(company);
    }
}
