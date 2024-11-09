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
        LocalDateTime dateTime = LocalDateTime.now();

        Inventory inventory = new Inventory(restaurant, dateTime, dateTime);

        // Mock the repository
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        Inventory savedInventory = inventoryService.saveInventory(inventory);

        // Then
        assertNotNull(savedInventory);
        assertEquals(restaurant.getId(), savedInventory.getRestaurant().getId());
        assertEquals(dateTime, savedInventory.getEmissionDate());
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

    @Test
    void testUpdateInventory_ShouldUpdateInventory() {
        UUID inventoryId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        Inventory existingInventory = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(7), "Old report");
        existingInventory.setId(inventoryId);

        Inventory updatedInventory = new Inventory(restaurant, LocalDateTime.now(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(8), "Updated report");
        updatedInventory.setId(inventoryId);

        // Mock the repository to return the existing inventory
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);

        // Call the updateInventory method
        Optional<Inventory> result = inventoryService.updateInventory(inventoryId, updatedInventory);

        assertTrue(result.isPresent());
        Inventory updatedResult = result.get();

        // Assertions to verify the update
        assertEquals("Updated report", updatedResult.getReport());
        assertEquals(updatedInventory.getEmissionDate(), updatedResult.getEmissionDate());
        assertEquals(updatedInventory.getClosingDate(), updatedResult.getClosingDate());
        assertEquals(updatedInventory.getExpectedClosingDate(), updatedResult.getExpectedClosingDate());

        verify(inventoryRepository, times(1)).save(updatedInventory);
    }

    @Test
    void testUpdateInventory_NotFound_ShouldReturnNull() {
        UUID inventoryId = UUID.randomUUID();
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(inventoryId);

        // Mock the repository to return an empty Optional
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // Call the updateInventory method
        Optional<Inventory> result = inventoryService.updateInventory(inventoryId, updatedInventory);

        // Assertions to verify the result is null
        assertFalse(result.isPresent());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

}
