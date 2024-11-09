package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.DAO.CreateInventoryRequest;
import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.services.InventoryService;
import com.supplywise.supplywise.services.RestaurantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@Tag(name = "Inventory Controller", description = "API for managing inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final RestaurantService restaurantService;
    private final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    public InventoryController(InventoryService inventoryService, RestaurantService restaurantService) {
        this.inventoryService = inventoryService;
        this.restaurantService = restaurantService;
    }

    @Operation(summary = "Create a new inventory", description = "Create a new inventory record for a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventory created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid inventory data")
    })
    @PostMapping("/")
    public ResponseEntity<Inventory> createInventory(@RequestBody CreateInventoryRequest createInventoryRequest) {
        logger.info("Attempting to create a new inventory");

        Optional<Restaurant> restaurantOptional = restaurantService.getRestaurantById(createInventoryRequest.getRestaurantId());
        if (!restaurantOptional.isPresent()) {
            logger.error("Restaurant not found");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Restaurant restaurant = restaurantOptional.get();
        Inventory inventory = new Inventory(
                restaurant,
                createInventoryRequest.getEmissionDate(),
                createInventoryRequest.getExpectedClosingDate()
        );

        Inventory savedInventory = inventoryService.saveInventory(inventory);
        logger.info("Inventory created successfully");
        return new ResponseEntity<>(savedInventory, HttpStatus.CREATED);
    }

    @Operation(summary = "Get inventory by ID", description = "Retrieve an inventory record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable UUID id) {
        logger.info("Attempting to get inventory by ID");
        Optional<Inventory> inventoryOptional = inventoryService.getInventoryById(id);

        if (inventoryOptional.isPresent()) {
            logger.info("Inventory found");
            return new ResponseEntity<>(inventoryOptional.get(), HttpStatus.OK);
        }
        logger.error("Inventory not found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get all inventories by restaurant", description = "Retrieve all inventory records associated with a specific restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventories retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No inventories found")
    })
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Inventory>> getInventoriesByRestaurant(@PathVariable UUID restaurantId) {
        logger.info("Attempting to get inventories by restaurant ID");

        Optional<Restaurant> restaurantOptional = restaurantService.getRestaurantById(restaurantId);
        if (!restaurantOptional.isPresent()) {
            logger.error("Restaurant not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Inventory> inventories = inventoryService.getInventoriesByRestaurant(restaurantOptional.get());
        if (inventories.isEmpty()) {
            logger.error("No inventories found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        logger.info("Inventories found");
        return new ResponseEntity<>(inventories, HttpStatus.OK);
    }

    @Operation(summary = "Get paginated item stocks by inventory ID", description = "Retrieve paginated item stocks associated with a specific inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item stocks retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No item stocks found")
    })
    @GetMapping("/{inventoryId}/item-stocks")
    public ResponseEntity<Page<ItemStock>> getItemStocksByInventoryId(
            @PathVariable UUID inventoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Attempting to get paginated item stocks by inventory ID");

        Optional<Inventory> inventoryOptional = inventoryService.getInventoryById(inventoryId);
        if (inventoryOptional.isPresent()) {
            Set<ItemStock> itemStocks = inventoryOptional.get().getItemStocks();
            
            // convert Set to List and apply pagination
            List<ItemStock> itemStockList = new ArrayList<>(itemStocks);
            int start = Math.min((int)PageRequest.of(page, size).getOffset(), itemStockList.size());
            int end = Math.min((start + size), itemStockList.size());
            
            if (start < end) {
                List<ItemStock> paginatedItemStocks = itemStockList.subList(start, end);
                Page<ItemStock> itemStockPage = new PageImpl<>(paginatedItemStocks, PageRequest.of(page, size), itemStockList.size());
                logger.info("Item stocks found");
                return new ResponseEntity<>(itemStockPage, HttpStatus.OK);
            }
        }

        logger.error("No item stocks found");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete inventory by ID", description = "Delete an existing inventory record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryById(@PathVariable UUID id) {
        logger.info("Attempting to delete inventory by ID");

        if (!inventoryService.getInventoryById(id).isPresent()) {
            logger.error("Inventory not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        inventoryService.deleteInventoryById(id);
        logger.info("Inventory deleted successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Update inventory by ID", description = "Update an existing inventory record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
            @ApiResponse(responseCode = "404", description = "Inventory not found"),
            @ApiResponse(responseCode = "400", description = "Invalid inventory data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable UUID id, @RequestBody Inventory inventoryDetails) {
        logger.info("Attempting to update inventory with ID: {}", id);

        Restaurant restaurant = inventoryDetails.getRestaurant();
        if (restaurant == null || !restaurantService.restaurantExistsById(restaurant.getId())) {
            logger.error("Invalid or missing restaurant");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Inventory> existingInventory = inventoryService.getInventoryById(id);
        if (!existingInventory.isPresent()) {
            logger.error("Inventory not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<Inventory> updatedInventory = inventoryService.updateInventory(id, inventoryDetails);
        if (updatedInventory.isPresent()) {
            logger.info("Inventory updated successfully");
            return new ResponseEntity<>(updatedInventory.get(), HttpStatus.OK);
        }

        logger.error("Inventory update failed");
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(summary = "Add item stock to inventory", description = "Add a new item stock to an existing inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item stock added successfully"),
            @ApiResponse(responseCode = "404", description = "Inventory not found"),
            @ApiResponse(responseCode = "400", description = "Invalid item stock data")
    })
    @PostMapping("/{inventoryId}/item-stocks")
    public ResponseEntity<Inventory> addItemStockToInventory(
            @PathVariable UUID inventoryId, @RequestBody ItemStock itemStock) {
    
        logger.info("Attempting to add item stock to inventory with ID: {}", inventoryId);
    
        // Check if the item stock is valid
        if (itemStock == null || itemStock.getQuantity() <= 0) {
            logger.error("Invalid or missing item stock data");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    
        Optional<Inventory> inventoryOptional = inventoryService.getInventoryById(inventoryId);
    
        if (!inventoryOptional.isPresent()) {
            logger.error("Inventory not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    
        Inventory inventory = inventoryOptional.get();
        inventory.addItemStock(itemStock);
        Inventory updatedInventory = inventoryService.saveInventory(inventory);
    
        logger.info("Item stock added successfully to inventory");
        return new ResponseEntity<>(updatedInventory, HttpStatus.OK);
    }
    
    @Operation(summary = "Check if inventory is closed", description = "Check if the inventory is closed by comparing today's date with the closing date of the inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory is closed or not closed", content = @Content(mediaType = "application/json", schema = @Schema(type = "boolean"))),
            @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    @GetMapping("/{id}/is-closed")
    public ResponseEntity<Boolean> isInventoryClosed(@PathVariable UUID id) {
        logger.info("Attempting to check if inventory with ID: {} is closed", id);

        Optional<Inventory> inventoryOptional = inventoryService.getInventoryById(id);
        if (!inventoryOptional.isPresent()) {
            logger.error("Inventory not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Inventory inventory = inventoryOptional.get();
        LocalDateTime closingDate = inventory.getClosingDate();

        // If closing date is not set, return false (inventory is not closed)
        if (closingDate == null) {
            logger.warn("Closing date not set for inventory");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        // Check if the current date and time is after the closing date
        boolean isClosed = LocalDateTime.now().isAfter(closingDate);
        logger.info("Inventory is closed: {}", isClosed);

        return new ResponseEntity<>(isClosed, HttpStatus.OK);
    }

    @Operation(summary = "Check if inventory should be closed", description = "Check if the inventory should be closed by comparing today's date with the expected closing date of the inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory should be closed or not", content = @Content(mediaType = "application/json", schema = @Schema(type = "boolean"))),
            @ApiResponse(responseCode = "404", description = "Inventory not found")
    })
    @GetMapping("/{id}/should-be-closed")
    public ResponseEntity<Boolean> isInventoryExpectedToClose(@PathVariable UUID id) {
        logger.info("Attempting to check if inventory with ID: {} should be closed", id);

        Optional<Inventory> inventoryOptional = inventoryService.getInventoryById(id);
        if (!inventoryOptional.isPresent()) {
            logger.error("Inventory not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Inventory inventory = inventoryOptional.get();
        LocalDateTime expectedClosingDate = inventory.getExpectedClosingDate();

        // If expected closing date is not set, return false (inventory should not be closed)
        if (expectedClosingDate == null) {
            logger.warn("Expected closing date not set for inventory");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        // Check if today's date is after the expected closing date
        boolean shouldBeClosed = LocalDateTime.now().isAfter(expectedClosingDate);
        logger.info("Inventory should be closed: {}", shouldBeClosed);

        return new ResponseEntity<>(shouldBeClosed, HttpStatus.OK);
    }

    @Operation(summary = "Get open inventories by restaurant", description = "Retrieve all open inventory records associated with a specific restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Open inventories retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No open inventories found"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @GetMapping("/restaurant/{restaurantId}/open")
    public ResponseEntity<List<Inventory>> getOpenInventoriesByRestaurant(@PathVariable UUID restaurantId) {
        logger.info("Attempting to get open inventories by restaurant ID");

        // Check if the restaurant exists
        Optional<Restaurant> restaurantOptional = restaurantService.getRestaurantById(restaurantId);
        if (!restaurantOptional.isPresent()) {
            logger.error("Restaurant not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Restaurant restaurant = restaurantOptional.get();

        // Fetch all inventories associated with the restaurant
        List<Inventory> inventories = inventoryService.getInventoriesByRestaurant(restaurant);
        
        // Filter inventories that are open (no closing date or closing date in the future)
        List<Inventory> openInventories = new ArrayList<>();
        for (Inventory inventory : inventories) {
            LocalDateTime closingDate = inventory.getClosingDate();
            
            // Inventory is open if no closing date is set or if the closing date is in the future
            if (closingDate == null || LocalDateTime.now().isBefore(closingDate)) {
                openInventories.add(inventory);
            }
        }

        // Return the open inventories
        if (openInventories.isEmpty()) {
            logger.info("No open inventories found");
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }

        logger.info("Open inventories found");
        return new ResponseEntity<>(openInventories, HttpStatus.OK);
    }

}
