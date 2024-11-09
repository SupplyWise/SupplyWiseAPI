package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.services.InventoryService;
import com.supplywise.supplywise.services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private InventoryController inventoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Helper method to create a sample inventory with restaurant
    private Inventory createInventory(UUID restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setRestaurant(restaurant);
        inventory.setEmissionDate(LocalDateTime.now());
        inventory.setClosingDate(LocalDateTime.now().plusDays(7));
        inventory.setExpectedClosingDate(LocalDateTime.now().plusDays(14));
        inventory.setReport("Test report");
        return inventory;
    }

    @Test
    void testCreateInventory_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Inventory inventory = createInventory(restaurantId);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.saveInventory(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/api/inventories/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.report").value("Test report"));

        verify(inventoryService, times(1)).saveInventory(any(Inventory.class));
    }

    @Test
    void testCreateInventory_InvalidRestaurant() throws Exception {
        Inventory inventory = new Inventory();  // Invalid restaurant (no restaurant ID)

        mockMvc.perform(post("/api/inventories/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }

    @Test
    void testGetInventoryById_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setReport("Test report");

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Test report"));

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testGetInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testGetInventoriesByRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        Inventory inventory1 = createInventory(restaurantId);
        Inventory inventory2 = createInventory(restaurantId);
        List<Inventory> inventories = new ArrayList<>(List.of(inventory1, inventory2));

        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].report").value("Test report"))
                .andExpect(jsonPath("$[1].report").value("Test report"));

        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testGetInventoriesByRestaurant_RestaurantNotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testGetInventoriesByRestaurant_NoContent() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testDeleteInventoryById_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(new Inventory()));

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).deleteInventoryById(eq(inventoryId));
    }

    @Test
    void testDeleteInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testUpdateInventory_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setRestaurant(restaurant);
        inventory.setEmissionDate(LocalDateTime.now());
        inventory.setClosingDate(LocalDateTime.now().plusDays(7));
        inventory.setExpectedClosingDate(LocalDateTime.now().plusDays(14));
        inventory.setReport("Updated report");

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(new Inventory()));
        when(inventoryService.updateInventory(eq(inventoryId), any(Inventory.class))).thenReturn(Optional.of(inventory));

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Updated report"));

        verify(inventoryService, times(1)).updateInventory(eq(inventoryId), any(Inventory.class));
    }

    @Test
    void testUpdateInventory_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setRestaurant(restaurant);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).updateInventory(eq(inventoryId), any(Inventory.class));
    }

    @Test
    void testAddItemStockToInventory_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);

        ItemStock itemStock = new ItemStock();
        itemStock.setQuantity(10);

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(inventory));
        when(inventoryService.saveInventory(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/api/inventories/" + inventoryId + "/item-stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemStock)))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).saveInventory(any(Inventory.class));
    }

    @Test
    void testAddItemStockToInventory_InventoryNotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        ItemStock itemStock = new ItemStock();
        itemStock.setQuantity(10);

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/inventories/" + inventoryId + "/item-stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemStock)))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }

    @Test
    void testAddItemStockToInventory_InvalidItemStockData() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);

        ItemStock invalidItemStock = new ItemStock();  // No quantity set

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(inventory));

        mockMvc.perform(post("/api/inventories/" + inventoryId + "/item-stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemStock)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }

    @Test
    void testIsInventoryClosed_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setClosingDate(LocalDateTime.now().minusDays(1)); // Set a past closing date to simulate closure

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/is-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true)); // Adjusted to expect a direct Boolean value

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testIsInventoryClosed_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/is-closed"))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testIsInventoryExpectedToClose_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setExpectedClosingDate(LocalDateTime.now().minusDays(1)); // Set a past expected closing date

        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/should-be-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true)); // Adjusted to expect a direct Boolean value

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testIsInventoryExpectedToClose_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(eq(inventoryId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/should-be-closed"))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(eq(inventoryId));
    }

    @Test
    void testGetOpenInventoriesByRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        
        // Create inventories (one open, one closed)
        Inventory openInventory = createInventory(restaurantId);
        openInventory.setClosingDate(LocalDateTime.now().plusDays(1));  // Set to future date
        
        Inventory closedInventory = createInventory(restaurantId);
        closedInventory.setClosingDate(LocalDateTime.now().minusDays(1));  // Set to past date
        
        List<Inventory> inventories = List.of(openInventory, closedInventory);

        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(eq(restaurant))).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].report").value("Test report"));

        verify(inventoryService, times(1)).getInventoriesByRestaurant(eq(restaurant));
    }

    @Test
    void testGetOpenInventoriesByRestaurant_NoOpenInventories() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        
        // Create closed inventories (all have past closing dates)
        Inventory closedInventory1 = createInventory(restaurantId);
        closedInventory1.setClosingDate(LocalDateTime.now().minusDays(1));
        
        Inventory closedInventory2 = createInventory(restaurantId);
        closedInventory2.setClosingDate(LocalDateTime.now().minusDays(2));
        
        List<Inventory> inventories = List.of(closedInventory1, closedInventory2);

        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(eq(restaurant))).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isNoContent());  // No open inventories found

        verify(inventoryService, times(1)).getInventoriesByRestaurant(eq(restaurant));
    }

    @Test
    void testGetOpenInventoriesByRestaurant_RestaurantNotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        
        // Mock that the restaurant does not exist
        when(restaurantService.getRestaurantById(eq(restaurantId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isNotFound());  // Restaurant not found

        verify(inventoryService, never()).getInventoriesByRestaurant(any(Restaurant.class));
    }


}
