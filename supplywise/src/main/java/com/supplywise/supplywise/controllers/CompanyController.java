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
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not eligible to create a company")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createCompany(@RequestParam String name) {

        logger.info("Attempting to create a new company");

        // Check if user is authenticated
        User user = authHandler.getAuthenticatedUser();
        if (user == null) {
            logger.error("User is not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
        }

        // Check if user is eligible to create a company
        if (user.getRole() != Role.DISASSOCIATED) {
            logger.error("User is not eligible to create a company");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not eligible to create a company.");
        }

        // Create a new company
        Company company = new Company(name, user.getId());
        companyService.createCompany(company);
        logger.info("Company created successfully");

        // Update user role to FRANCHISE_OWNER
        user.setRole(Role.FRANCHISE_OWNER);
        userService.updateUser(user.getId(), user);
        logger.info("Role updated to FRANCHISE_OWNER");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Company created successfully. Role updated to FRANCHISE_OWNER.");
    }
}
