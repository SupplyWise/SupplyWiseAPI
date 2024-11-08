package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.services.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/create")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        logger.info("Request to create Item: {}", item);
        Item createdItem = itemService.createItem(item);
        logger.info("Created Item with ID: {}", createdItem.getId());
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        logger.info("Request to get all Items");
        List<Item> items = itemService.getAllItems();
        logger.info("Found {} Items", items.size());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable UUID id) {
        logger.info("Request to get Item by ID: {}", id);
        Item item = itemService.getItemById(id);
        if (item != null) {
            logger.info("Found Item with ID: {}", id);
            return ResponseEntity.ok(item);
        } else {
            logger.warn("Item not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable UUID id, @RequestBody Item itemDetails) {
        logger.info("Request to update Item with ID: {}", id);
        Item updatedItem = itemService.updateItem(id, itemDetails);
        if (updatedItem != null) {
            logger.info("Updated Item with ID: {}", id);
            return ResponseEntity.ok(updatedItem);
        } else {
            logger.warn("Item not found for update with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        logger.info("Request to delete Item with ID: {}", id);
        itemService.deleteItem(id);
        logger.info("Deleted Item with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
