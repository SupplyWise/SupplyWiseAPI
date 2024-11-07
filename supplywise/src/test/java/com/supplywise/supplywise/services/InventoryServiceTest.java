package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveInventory_ShouldSaveInventory() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());

        Inventory inventory = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Test report");

        // Mock the repository
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        Inventory savedInventory = inventoryService.saveInventory(inventory);

        // Then
        assertNotNull(savedInventory);
        assertEquals(restaurant.getId(), savedInventory.getRestaurant().getId());
        assertEquals("Test report", savedInventory.getReport());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testGetInventoryById_Exists_ShouldReturnInventory() {
        // Given
        UUID inventoryId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        Inventory inventory = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Test report");
        inventory.setId(inventoryId);

        // Mock the repository
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        // When
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(inventoryId, result.get().getId());
        assertEquals("Test report", result.get().getReport());
    }

    @Test
    void testGetInventoryById_NotExists_ShouldReturnEmpty() {
        // Given
        UUID inventoryId = UUID.randomUUID();

        // Mock the repository
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetInventoriesByRestaurant_Exists_ShouldReturnList() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        Inventory inventory1 = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Report 1");
        Inventory inventory2 = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "Report 2");
        List<Inventory> inventories = new ArrayList<>(List.of(inventory1, inventory2));

        // Mock the repository
        when(inventoryRepository.findByRestaurant(restaurant)).thenReturn(inventories);

        // When
        List<Inventory> result = inventoryService.getInventoriesByRestaurant(restaurant);

        // Then
        assertEquals(2, result.size());
        assertEquals("Report 1", result.get(0).getReport());
        assertEquals("Report 2", result.get(1).getReport());
    }

    @Test
    void testGetInventoriesByRestaurant_NotExists_ShouldReturnEmptyList() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        when(inventoryRepository.findByRestaurant(restaurant)).thenReturn(new ArrayList<>());

        // When
        List<Inventory> result = inventoryService.getInventoriesByRestaurant(restaurant);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteInventoryById_Exists_ShouldDelete() {
        // Given
        UUID inventoryId = UUID.randomUUID();
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(new Inventory()));

        // When
        inventoryService.deleteInventoryById(inventoryId);

        // Then
        verify(inventoryRepository, times(1)).deleteById(inventoryId);
    }

    @Test
    void testDeleteInventoryById_NotExists_ShouldNotDelete() {
        // Given
        UUID inventoryId = UUID.randomUUID();
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When
        inventoryService.deleteInventoryById(inventoryId);

        // Then
        verify(inventoryRepository, never()).deleteById(any());
    }
}
