package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemStockServiceTest {

    @Mock
    private ItemStockRepository itemStockRepository;

    @InjectMocks
    private ItemStockService itemStockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveItemStock_ValidItemProperties_ShouldSaveItemStock() {
        // Given
        UUID itemPropertiesId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        itemProperties.setId(itemPropertiesId);

        ItemStock itemStock = new ItemStock(100, itemProperties);

        when(itemStockRepository.save(any(ItemStock.class))).thenReturn(itemStock);

        // When
        ItemStock savedItemStock = itemStockService.saveItemStock(itemStock);

        // Then
        assertNotNull(savedItemStock);
        assertEquals(100, savedItemStock.getQuantity());
        verify(itemStockRepository, times(1)).save(any(ItemStock.class));
    }

    @Test
    void testSaveItemStock_InvalidItemProperties_ShouldThrowException() {
        // Given
        ItemStock itemStock = new ItemStock(100, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> itemStockService.saveItemStock(itemStock));
        verify(itemStockRepository, never()).save(any(ItemStock.class));
    }

    @Test
    void testGetItemStockById_Exists_ShouldReturnItemStock() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, itemProperties);
        itemStock.setId(itemStockId);

        // Mock the repository
        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.of(itemStock));

        // When
        Optional<ItemStock> result = itemStockService.getItemStockById(itemStockId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(itemStockId, result.get().getId());
        assertEquals(100, result.get().getQuantity());
    }

    @Test
    void testGetItemStockById_NotExists_ShouldReturnEmpty() {
        // Given
        UUID itemStockId = UUID.randomUUID();

        // Mock the repository
        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.empty());

        // When
        Optional<ItemStock> result = itemStockService.getItemStockById(itemStockId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateItemStockQuantity_Exists_ShouldUpdateQuantity() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, itemProperties);
        itemStock.setId(itemStockId);

        // Mock the repository
        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.of(itemStock));
        when(itemStockRepository.save(any(ItemStock.class))).thenReturn(itemStock);

        // When
        Optional<ItemStock> result = itemStockService.updateItemStockQuantity(itemStockId, 200);

        // Then
        assertTrue(result.isPresent());
        assertEquals(200, result.get().getQuantity());
        verify(itemStockRepository, times(1)).save(itemStock);
    }

    @Test
    void testUpdateItemStockQuantity_NotExists_ShouldReturnEmpty() {
        // Given
        UUID itemStockId = UUID.randomUUID();

        // Mock the repository
        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.empty());

        // When
        Optional<ItemStock> result = itemStockService.updateItemStockQuantity(itemStockId, 200);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateMinimumQuantity_Exists_ShouldUpdateMinimumQuantity() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, 50, itemProperties);
        itemStock.setId(itemStockId);

        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.of(itemStock));
        when(itemStockRepository.save(any(ItemStock.class))).thenReturn(itemStock);

        // When
        Optional<ItemStock> result = itemStockService.updateMinimumQuantity(itemStockId, 125);

        // Then
        assertTrue(result.isPresent());
        assertEquals(125, result.get().getMinimumQuantity());
        assertTrue(result.get().isLowStock()); // quantity (100) < minimumQuantity (125)
        verify(itemStockRepository, times(1)).save(itemStock);
    }

    @Test
    void testUpdateMinimumQuantity_NotExists_ShouldReturnEmpty() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.empty());

        // When
        Optional<ItemStock> result = itemStockService.updateMinimumQuantity(itemStockId, 50);

        // Then
        assertFalse(result.isPresent());
        verify(itemStockRepository, never()).save(any(ItemStock.class));
    }

    @Test
    void testLowStockFlag_UpdateQuantityBelowMinimum_ShouldSetLowStockTrue() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, 50, itemProperties);
        itemStock.setId(itemStockId);

        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.of(itemStock));
        when(itemStockRepository.save(any(ItemStock.class))).thenReturn(itemStock);

        // When
        Optional<ItemStock> result = itemStockService.updateItemStockQuantity(itemStockId, 40);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().isLowStock());
        verify(itemStockRepository, times(1)).save(itemStock);
    }

    @Test
    void testLowStockFlag_UpdateQuantityAboveMinimum_ShouldSetLowStockFalse() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(40, 50, itemProperties);
        itemStock.setId(itemStockId);
        itemStock.setLowStock(true);

        when(itemStockRepository.findById(itemStockId)).thenReturn(Optional.of(itemStock));
        when(itemStockRepository.save(any(ItemStock.class))).thenReturn(itemStock);

        // When
        Optional<ItemStock> result = itemStockService.updateItemStockQuantity(itemStockId, 60);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().isLowStock());
        verify(itemStockRepository, times(1)).save(itemStock);
    }

    @Test
    void testDeleteItemStockById_Exists_ShouldDelete() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        when(itemStockRepository.existsById(itemStockId)).thenReturn(true);

        // When
        itemStockService.deleteItemStockById(itemStockId);

        // Then
        verify(itemStockRepository, times(1)).deleteById(itemStockId);
    }

    @Test
    void testDeleteItemStockById_NotExists_ShouldNotDelete() {
        // Given
        UUID itemStockId = UUID.randomUUID();
        when(itemStockRepository.existsById(itemStockId)).thenReturn(false);

        // When
        itemStockService.deleteItemStockById(itemStockId);

        // Then
        verify(itemStockRepository, never()).deleteById(any());
    }

}
