package com.supplywise.supplywise.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supplywise.supplywise.DAO.HealthCheckResponse;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Controller", description = "Health check endpoint")
public class HealthCheckController {

    private final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Operation(summary = "Health Check", description = "Returns a simple health check response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health check successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthCheckResponse.class))),
    })
    @GetMapping
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        logger.info("Health check request received");
        return ResponseEntity.ok(new HealthCheckResponse("SupplyWiseAPI is healthy"));
    }
}