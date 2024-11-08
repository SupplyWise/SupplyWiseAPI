package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemPropertiesService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-properties")
public class ItemPropertiesController {

    private static final Logger logger = LoggerFactory.getLogger(ItemPropertiesController.class);

    private final ItemPropertiesService itemPropertiesService;
    private final AuthHandler authHandler;

    @Autowired
    public ItemPropertiesController(ItemPropertiesService itemPropertiesService, AuthHandler authHandler) {
        this.itemPropertiesService = itemPropertiesService;
        this.authHandler = authHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItemProperties(@RequestBody ItemProperties itemProperties) {
        logger.info("Attempting to create item properties");

        User authenticatedUser = authHandler.getAuthenticatedUser();
        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to create item properties");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to create item properties.");
        }

        ItemProperties createdItemProperties = itemPropertiesService.createItemProperties(itemProperties);
        logger.info("Item properties created successfully with ID: {}", createdItemProperties.getId());
        return new ResponseEntity<>(createdItemProperties, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemProperties>> getAllItemProperties() {
        logger.info("Fetching all item properties");

        List<ItemProperties> itemPropertiesList = itemPropertiesService.getAllItemProperties();
        logger.info("Fetched {} item properties", itemPropertiesList.size());
        return ResponseEntity.ok(itemPropertiesList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemPropertiesById(@PathVariable UUID id) {
        logger.info("Attempting to fetch item properties with ID: {}", id);

        User authenticatedUser = authHandler.getAuthenticatedUser();
        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to fetch item properties");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to fetch item properties.");
        }

        ItemProperties itemProperties = itemPropertiesService.getItemPropertiesById(id);


        if (itemProperties == null) {
            logger.error("Item properties not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item Properties ID does not exist.");
        }

        logger.info("Item properties fetched successfully with ID: {}", id);
        return ResponseEntity.ok(itemProperties);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItemProperties(@PathVariable UUID id) {
        logger.info("Attempting to delete item properties with ID: {}", id);

        User authenticatedUser = authHandler.getAuthenticatedUser();
        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to delete item properties");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to delete item properties.");
        }

        itemPropertiesService.deleteItemProperties(id);
        logger.info("Item properties deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
