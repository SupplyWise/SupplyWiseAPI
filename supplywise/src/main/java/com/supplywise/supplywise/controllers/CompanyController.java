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
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/create")
    public ResponseEntity<String> createCompany(@RequestParam String name) {

        logger.info("Attempting to create a new company");

        User user = authHandler.getAuthenticatedUser();

        // Check if user is eligible to create a company
        if (user.getRole() != Role.DISASSOCIATED) {
            logger.error("User is not eligible to create a company");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not eligible to create a company.");
        }

        // Create a new company
        Company company = new Company(name, user.getId());
        companyService.createCompany(company);
        logger.info("Company created successfully");

        // Update user company
        user.setCompany(company);
        userService.updateUser(user.getId(), user);
        logger.info("User company updated");

        // Update user role to FRANCHISE_OWNER
        user.setRole(Role.FRANCHISE_OWNER);
        userService.updateUser(user.getId(), user);
        logger.info("Role updated to FRANCHISE_OWNER");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Company created successfully. Role updated to FRANCHISE_OWNER.");
    }

    @Operation(summary = "Get company details", description = "Get the details of a company by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company details fetched successfully"),
            @ApiResponse(responseCode = "403", description = "User is not eligible to view company details"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/details/{id}")
    public ResponseEntity<String> getCompanyDetails(@PathVariable UUID id) {

        logger.info("Attempting to fetch company details");

        // Get the authenticated user
        User authenticatedUser = authHandler.getAuthenticatedUser();

        // Check if company exists
        Company company = userService.getCompanyDetails(id);
        if (company == null) {
            logger.error("Company not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company ID does not exist.");
        }
        
        // Check if the user is eligible to view company details
        if (!authenticatedUser.getCompany().getId().equals(id) && authenticatedUser.getRole() != Role.ADMIN) {
            logger.error("User is not eligible to view company details");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not eligible to view company details.");
        }

        logger.info("Company details fetched successfully");
        return ResponseEntity.ok(company.toString());
    }
}
