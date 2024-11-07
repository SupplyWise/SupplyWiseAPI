package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.services.InventoryService;
import com.supplywise.supplywise.services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void testCreateInventory_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setRestaurant(restaurant);
        inventory.setEmissionDate(LocalDateTime.now());
        inventory.setClosingDate(LocalDateTime.now());
        inventory.setExpectedClosingDate(LocalDateTime.now());
        inventory.setReport("Test report");

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
        Inventory inventory = new Inventory();
        inventory.setRestaurant(new Restaurant()); // Setting an invalid restaurant without an ID

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

        when(inventoryService.getInventoryById(any(UUID.class))).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Test report"));
        
        verify(inventoryService, times(1)).getInventoryById(any(UUID.class));
    }

    @Test
    void testGetInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());
        
        verify(inventoryService, times(1)).getInventoryById(any(UUID.class));
    }

    @Test
    void testGetInventoriesByRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        Inventory inventory1 = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Report 1");
        Inventory inventory2 = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Report 2");
        List<Inventory> inventories = new ArrayList<>(List.of(inventory1, inventory2));

        when(restaurantService.getRestaurantById(any(UUID.class))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].report").value("Report 1"))
                .andExpect(jsonPath("$[1].report").value("Report 2"));
        
        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testGetInventoriesByRestaurant_RestaurantNotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.getRestaurantById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNotFound());
        
        verify(inventoryService, never()).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testGetInventoriesByRestaurant_NoContent() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        when(restaurantService.getRestaurantById(any(UUID.class))).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNoContent());
        
        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    void testDeleteInventoryById_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(any(UUID.class))).thenReturn(Optional.of(new Inventory()));

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isOk());
        
        verify(inventoryService, times(1)).deleteInventoryById(any(UUID.class));
    }

    @Test
    void testDeleteInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());
        
        verify(inventoryService, times(1)).getInventoryById(any(UUID.class));
    }
}
