package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.services.RestaurantService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurant Controller", description = "API for managing restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final CompanyService companyService;
    private final AuthHandler authHandler;
    private final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    private static final String RESTAURANT_NOT_FOUND = "Restaurant not found";

    @Autowired
    public RestaurantController(RestaurantService restaurantService, CompanyService companyService, AuthHandler authHandler) {
        this.restaurantService = restaurantService;
        this.companyService = companyService;
        this.authHandler = authHandler;
    }

    @Operation(summary = "Create a new restaurant", description = "Create a new restaurant for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid restaurant data")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER')")
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        logger.info("Attempting to create a new restaurant");

        Company company = restaurant.getCompany();
        if (company == null || !companyService.companyExists(company.getId())) {
            logger.error("Invalid or missing company");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);
        logger.info("Restaurant created successfully");
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    @Operation(summary = "Get restaurant by ID", description = "Retrieve a restaurant by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurant found",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = Restaurant.class))),
        @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable UUID id) {
        logger.info("Attempting to get restaurant by ID");
        Optional<Restaurant> restaurantOptional = restaurantService.getRestaurantById(id);

        if (restaurantOptional.isPresent()) {
            logger.info("Restaurant found");
            return new ResponseEntity<>(restaurantOptional.get(), HttpStatus.OK);
        }
        logger.error(RESTAURANT_NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update restaurant name by ID", description = "Update an existing restaurant's name by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurant updated successfully"),
        @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable UUID id, @RequestBody String newName) {
        logger.info("Attempting to update restaurant name");

        Optional<Restaurant> updatedRestaurant = restaurantService.updateRestaurantName(id, newName);

        if (!updatedRestaurant.isPresent()) {
            logger.error(RESTAURANT_NOT_FOUND);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.info("Restaurant name updated successfully");
        return new ResponseEntity<>(updatedRestaurant.get(), HttpStatus.OK);
    }

    @Operation(summary = "Delete restaurant by ID", description = "Delete an existing restaurant by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurant deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurantById(@PathVariable UUID id) {
        logger.info("Attempting to delete restaurant by ID");

        if (!restaurantService.restaurantExistsById(id)) {
            logger.error(RESTAURANT_NOT_FOUND);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        restaurantService.deleteRestaurantById(id);
        logger.info("Restaurant deleted successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get all restaurants by company ID", description = "Retrieve all restaurants associated with a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No restaurants found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByCompanyId(@PathVariable UUID companyId) {
        logger.info("Attempting to get restaurants by company ID");

        List<Restaurant> restaurants = restaurantService.getRestaurantsByCompanyId(companyId);
        if (restaurants.isEmpty()) {
            logger.error("No restaurants found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        logger.info("Restaurants found");
        return new ResponseEntity<>(restaurants, HttpStatus.OK);
    }

    @Operation(summary = "Get restaurants", description = "Retrieve all restaurants associated with the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No restaurants found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER_MASTER', 'ROLE_MANAGER')")
    @GetMapping("/company")
    public ResponseEntity<List<Restaurant>> getRestaurants() {
        logger.info("Attempting to get restaurants for the authenticated user");

        // Check if the authenticated user is an admin or franchise owner
        if (authHandler.hasRole("ROLE_ADMIN") || authHandler.hasRole("ROLE_FRANCHISE_OWNER")) {
            UUID restaurantId = UUID.fromString(authHandler.getAuthenticatedRestaurantId());
            Optional<Restaurant> restaurant = restaurantService.getRestaurantById(restaurantId);

            if (restaurant.isPresent()) {
                logger.info("Restaurant found");
                return new ResponseEntity<>(List.of(restaurant.get()), HttpStatus.OK);
            }
            logger.error(RESTAURANT_NOT_FOUND);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            UUID companyId = UUID.fromString(authHandler.getAuthenticatedCompanyId());
            logger.info("Company ID: {}", companyId);
    
            List<Restaurant> restaurants = restaurantService.getRestaurantsByCompanyId(companyId);
            if (restaurants.isEmpty()) {
                logger.error("No restaurants found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
    
            logger.info("Restaurants found");
            return new ResponseEntity<>(restaurants, HttpStatus.OK);
        }
    }
}
