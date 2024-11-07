package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemStockService;
import com.supplywise.supplywise.services.ItemPropertiesService;

import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-stock")
@Tag(name = "Item Stock Controller", description = "API for managing item stock")
public class ItemStockController {

    private final ItemStockService itemStockService;
    private final ItemPropertiesService itemPropertiesService;
    private final Logger logger = LoggerFactory.getLogger(ItemStockController.class);

    @Autowired
    public ItemStockController(ItemStockService itemStockService, ItemPropertiesService itemPropertiesService) {
        this.itemStockService = itemStockService;
        this.itemPropertiesService = itemPropertiesService;
    }

    @Operation(summary = "Create a new item stock", description = "Create a new item stock for a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item stock created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid item stock data")
    })
    @PostMapping("/")
    public ResponseEntity<ItemStock> createItemStock(@RequestBody ItemStock itemStock) {
        logger.info("Attempting to create a new item stock");

        try {
            ItemStock savedItemStock = itemStockService.saveItemStock(itemStock);
            logger.info("Item stock created successfully");
            return new ResponseEntity<>(savedItemStock, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid item stock data: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get item stock by ID", description = "Retrieve an item stock by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item stock found",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemStock.class))),
        @ApiResponse(responseCode = "404", description = "Item stock not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemStock> getItemStockById(@PathVariable UUID id) {
        logger.info("Attempting to get item stock by ID");
        Optional<ItemStock> itemStockOptional = itemStockService.getItemStockById(id);

        if (itemStockOptional.isPresent()) {
            logger.info("Item stock found");
            return new ResponseEntity<>(itemStockOptional.get(), HttpStatus.OK);
        }
        logger.error("Item stock not found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update item stock quantity by ID", description = "Update an existing item stock's quantity by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item stock updated successfully"),
        @ApiResponse(responseCode = "404", description = "Item stock not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemStock> updateItemStock(@PathVariable UUID id, @RequestParam int newQuantity) {
        logger.info("Attempting to update item stock quantity");

        Optional<ItemStock> updatedItemStock = itemStockService.updateItemStockQuantity(id, newQuantity);

        if (!updatedItemStock.isPresent()) {
            logger.error("Item stock not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.info("Item stock quantity updated successfully");
        return new ResponseEntity<>(updatedItemStock.get(), HttpStatus.OK);
    }

    @Operation(summary = "Delete item stock by ID", description = "Delete an existing item stock by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item stock deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Item stock not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemStockById(@PathVariable UUID id) {
        logger.info("Attempting to delete item stock by ID");

        if (!itemStockService.itemStockExistsById(id)) {
            logger.error("Item stock not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        itemStockService.deleteItemStockById(id);
        logger.info("Item stock deleted successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}