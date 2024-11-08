package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.services.ItemService;
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
@RequestMapping("/api/item")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final AuthHandler authHandler;

    @Autowired
    public ItemController(ItemService itemService, AuthHandler authHandler) {
        this.itemService = itemService;
        this.authHandler = authHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody Item item) {
        logger.info("Attempting to create item");

        User authenticatedUser = authHandler.getAuthenticatedUser();
        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to create items");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to create items.");
        }

        Item createdItem = itemService.createItem(item);
        logger.info("Item created successfully with ID: {}", createdItem.getId());
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        logger.info("Fetching all items");

        List<Item> items = itemService.getAllItems();
        logger.info("Fetched {} items", items.size());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable UUID id) {
        logger.info("Attempting to fetch item with ID: {}", id);

        User authenticatedUser = authHandler.getAuthenticatedUser();

        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to fetch items");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to fetch items.");
        }

        Item item = itemService.getItemById(id);

        if (item == null) {
            logger.error("Item not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item ID does not exist.");
        }

        logger.info("Item fetched successfully with ID: {}", id);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable UUID id) {
        logger.info("Attempting to delete item with ID: {}", id);

        User authenticatedUser = authHandler.getAuthenticatedUser();
        if (authenticatedUser.getRole() == Role.DISASSOCIATED) {
            logger.error("User is not authorized to delete items");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to delete items.");
        }

        itemService.deleteItem(id);
        logger.info("Item deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
