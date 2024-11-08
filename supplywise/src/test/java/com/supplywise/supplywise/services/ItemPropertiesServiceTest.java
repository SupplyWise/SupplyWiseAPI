package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemPropertiesServiceTest {

    @Mock
    private ItemPropertiesRepository itemPropertiesRepository;

    @InjectMocks
    private ItemPropertiesService itemPropertiesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateItemProperties_ShouldSaveItemProperties() {
        // Mock Item
        Item item = new Item();
        item.setId(UUID.randomUUID());

        // ItemProperties data
        ItemProperties itemProperties = new ItemProperties();
        itemProperties.setItem(item);
        itemProperties.setExpirationDate(LocalDate.of(2025, 12, 31));
        itemProperties.setQuantity(100);

        // Mock the repository to return the itemProperties when saved
        when(itemPropertiesRepository.save(any(ItemProperties.class))).thenReturn(itemProperties);

        // Execute the method
        ItemProperties createdItemProperties = itemPropertiesService.createItemProperties(itemProperties);

        // Verify that the itemProperties is saved
        verify(itemPropertiesRepository, times(1)).save(itemProperties);

        // Check if the itemProperties returned matches the mock
        assertEquals(item, createdItemProperties.getItem());
        assertEquals(LocalDate.of(2025, 12, 31), createdItemProperties.getExpirationDate());
        assertEquals(100, createdItemProperties.getQuantity());
    }

    @Test
    void testGetItemPropertiesById_ItemFound_ShouldReturnItemProperties() {
        // Generate a random UUID for the itemProperties
        UUID itemPropertiesId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        itemProperties.setId(itemPropertiesId);
        itemProperties.setItem(new Item());
        itemProperties.setExpirationDate(LocalDate.of(2025, 12, 31));
        itemProperties.setQuantity(100);

        // Mock the repository to return the itemProperties when searched by ID
        when(itemPropertiesRepository.findById(itemPropertiesId)).thenReturn(Optional.of(itemProperties));

        // Execute the method
        Optional<ItemProperties> foundItemProperties = Optional.ofNullable(itemPropertiesService.getItemPropertiesById(itemPropertiesId));

        // Check if the itemProperties is returned
        assertTrue(foundItemProperties.isPresent());
        assertEquals(itemPropertiesId, foundItemProperties.get().getId());
        assertEquals(LocalDate.of(2025, 12, 31), foundItemProperties.get().getExpirationDate());
        assertEquals(100, foundItemProperties.get().getQuantity());

        // Verify that the findById method was called
        verify(itemPropertiesRepository, times(1)).findById(itemPropertiesId);
    }

    @Test
    void testGetItemPropertiesById_ItemNotFound_ShouldReturnEmpty() {
        // Generate a random UUID for the itemProperties
        UUID itemPropertiesId = UUID.randomUUID();

        // Mock the repository to return an empty result when searching by ID
        when(itemPropertiesRepository.findById(itemPropertiesId)).thenReturn(Optional.empty());

        // Execute the method
        Optional<ItemProperties> foundItemProperties = Optional.ofNullable(itemPropertiesService.getItemPropertiesById(itemPropertiesId));

        // Check if no itemProperties is found
        assertFalse(foundItemProperties.isPresent());

        // Verify that the findById method was called
        verify(itemPropertiesRepository, times(1)).findById(itemPropertiesId);
    }

    @Test
    void testGetAllItemProperties_ShouldReturnAllItemProperties() {
        // Mock ItemProperties list
        ItemProperties itemProperties1 = new ItemProperties();
        itemProperties1.setId(UUID.randomUUID());
        itemProperties1.setItem(new Item());
        itemProperties1.setExpirationDate(LocalDate.of(2025, 12, 31));
        itemProperties1.setQuantity(100);

        ItemProperties itemProperties2 = new ItemProperties();
        itemProperties2.setId(UUID.randomUUID());
        itemProperties2.setItem(new Item());
        itemProperties2.setExpirationDate(LocalDate.of(2026, 12, 31));
        itemProperties2.setQuantity(200);

        List<ItemProperties> itemPropertiesList = List.of(itemProperties1, itemProperties2);

        // Mock the repository to return the list of itemProperties
        when(itemPropertiesRepository.findAll()).thenReturn(itemPropertiesList);

        // Execute the method
        List<ItemProperties> foundItemPropertiesList = itemPropertiesService.getAllItemProperties();

        // Check if the list returned matches the mock
        assertEquals(2, foundItemPropertiesList.size());
        assertEquals(itemProperties1.getId(), foundItemPropertiesList.get(0).getId());
        assertEquals(itemProperties2.getId(), foundItemPropertiesList.get(1).getId());

        // Verify that the findAll method was called
        verify(itemPropertiesRepository, times(1)).findAll();
    }

    @Test
    void testUpdateItemProperties_ShouldUpdateAndReturnUpdatedItemProperties() {
        // Mock Item
        Item item = new Item();
        item.setId(UUID.randomUUID());

        // Existing ItemProperties data
        UUID itemPropertiesId = UUID.randomUUID();
        ItemProperties existingItemProperties = new ItemProperties();
        existingItemProperties.setId(itemPropertiesId);
        existingItemProperties.setItem(item);
        existingItemProperties.setExpirationDate(LocalDate.of(2025, 12, 31));
        existingItemProperties.setQuantity(100);

        // Updated ItemProperties data
        ItemProperties updatedItemProperties = new ItemProperties();
        updatedItemProperties.setItem(item);
        updatedItemProperties.setExpirationDate(LocalDate.of(2026, 12, 31));
        updatedItemProperties.setQuantity(200);

        // Mock the repository to return the existing itemProperties when searched by ID
        when(itemPropertiesRepository.findById(itemPropertiesId)).thenReturn(Optional.of(existingItemProperties));
        // Mock the repository to return the updated itemProperties when saved
        when(itemPropertiesRepository.save(any(ItemProperties.class))).thenReturn(updatedItemProperties);

        // Execute the method
        ItemProperties result = itemPropertiesService.updateItemProperties(itemPropertiesId, updatedItemProperties);

        // Verify that the itemProperties is updated and saved
        verify(itemPropertiesRepository, times(1)).findById(itemPropertiesId);
        verify(itemPropertiesRepository, times(1)).save(existingItemProperties);

        // Check if the itemProperties returned matches the updated data
        assertEquals(item, result.getItem());
        assertEquals(LocalDate.of(2026, 12, 31), result.getExpirationDate());
        assertEquals(200, result.getQuantity());
    }

    @Test
    void testDeleteItemProperties_ShouldDeleteItemProperties() {
        // Generate a random UUID for the itemProperties
        UUID itemPropertiesId = UUID.randomUUID();

        // Mock the repository to do nothing when deleting
        doNothing().when(itemPropertiesRepository).deleteById(itemPropertiesId);

        // Execute the method
        itemPropertiesService.deleteItemProperties(itemPropertiesId);

        // Verify that the deleteById method was called
        verify(itemPropertiesRepository, times(1)).deleteById(itemPropertiesId);
    }
}
