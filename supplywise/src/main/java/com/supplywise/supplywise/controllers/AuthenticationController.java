package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.services.AuthHandler;

import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Public API for managing user authentication")
public class AuthenticationController {

    private final AuthHandler authHandler;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    public AuthenticationController(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Operation(summary = "Get user roles", description = "Get the roles of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not authenticated")
    })
    @GetMapping("/roles")
    public ResponseEntity<String> getUserRoles() {

        logger.info("Attempting to fetch user roles");

        // Get the roles of the currently authenticated user
        String roles = authHandler.getAuthenticatedUserRoles().toString();

        logger.info("User roles: {}", roles);

        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

}
