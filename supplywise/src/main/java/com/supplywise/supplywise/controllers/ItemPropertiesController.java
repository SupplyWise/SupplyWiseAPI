package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemPropertiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-properties")
public class ItemPropertiesController {

    private static final Logger logger = LoggerFactory.getLogger(ItemPropertiesController.class);

    private final ItemPropertiesService itemPropertiesService;

    @Autowired
    public ItemPropertiesController(ItemPropertiesService itemPropertiesService) {
        this.itemPropertiesService = itemPropertiesService;
    }

    @Operation(summary = "Create new item properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item properties created successfully"),
            @ApiResponse(responseCode = "400", description = "Item properties is not valid"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to create item properties")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @PostMapping("/create")
    public ResponseEntity<Object> createItemProperties(@RequestBody ItemProperties itemProperties) {
        logger.info("Attempting to create item properties");

        try {
            ItemProperties createdItemProperties = itemPropertiesService.createItemProperties(itemProperties);
            logger.info("Item properties created successfully with ID: {}", createdItemProperties.getId());
            return new ResponseEntity<>(createdItemProperties, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Item properties is not valid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item properties is not valid.");
        }
    }

    @Operation(summary = "Get all item properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched all item properties successfully")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @GetMapping
    public ResponseEntity<List<ItemProperties>> getAllItemProperties() {
        logger.info("Fetching all item properties");

        List<ItemProperties> itemPropertiesList = itemPropertiesService.getAllItemProperties();
        logger.info("Fetched {} item properties", itemPropertiesList.size());
        return ResponseEntity.ok(itemPropertiesList);
    }

    @Operation(summary = "Get item properties by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item properties fetched successfully"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to fetch item properties"),
            @ApiResponse(responseCode = "404", description = "Item properties ID does not exist")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemPropertiesById(@Parameter(description = "ID of the item properties to be fetched") @PathVariable UUID id) {
        logger.info("Attempting to fetch item properties with ID: {}", id);

        ItemProperties itemProperties = itemPropertiesService.getItemPropertiesById(id);

        if (itemProperties == null) {
            logger.error("Item properties not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item properties ID does not exist.");
        }

        logger.info("Item properties fetched successfully with ID: {}", id);
        return ResponseEntity.ok(itemProperties);
    }

    @Operation(summary = "Update item properties by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item properties updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid item properties fields"),
            @ApiResponse(responseCode = "404", description = "Item properties not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER_MASTER')")
    public ResponseEntity<Object> updateItemProperties(
            @PathVariable UUID id, 
            @RequestBody ItemProperties itemProperties) {

        logger.info("Attempting to update item properties for ID: {}", id);

        // Validate required fields for all users
        if (itemProperties.getExpirationDate() == null || itemProperties.getQuantity() == null || itemProperties.getItem() == null) {
            logger.error("Item properties fields are missing or invalid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item properties fields are missing or invalid.");
        }

        try {
            ItemProperties updatedItemProperties = itemPropertiesService.updateItemProperties(id, itemProperties);

            if (updatedItemProperties == null) {
                logger.error("Item properties not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item properties not found.");
            }

            logger.info("Item properties updated successfully with ID: {}", id);
            logger.info("Item properties: {}", updatedItemProperties);
            return ResponseEntity.ok(updatedItemProperties);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid item properties: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete item properties by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item properties deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to delete item properties")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItemProperties(@Parameter(description = "ID of the item properties to be deleted") @PathVariable UUID id) {
        logger.info("Attempting to delete item properties with ID: {}", id);

        itemPropertiesService.deleteItemProperties(id);
        logger.info("Item properties deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}