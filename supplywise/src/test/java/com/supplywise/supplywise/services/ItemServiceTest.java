package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.repositories.ItemRepository;
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

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateItem_ShouldSaveItem() {
        // Item data
        Item item = new Item();
        item.setName("Test Item");

        // Mock the repository to return the item when saved
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Execute the method
        Item createdItem = itemService.createItem(item);

        // Verify that the item is saved
        verify(itemRepository, times(1)).save(item);

        // Check if the item returned matches the mock
        assertEquals("Test Item", createdItem.getName());
    }

    @Test
    void testGetItemById_ItemFound_ShouldReturnItem() {
        // Generate a random UUID for the item
        UUID itemId = UUID.randomUUID();
        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");

        // Mock the repository to return the item when searched by ID
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Execute the method
        Optional<Item> foundItem = Optional.ofNullable(itemService.getItemById(itemId));

        // Check if the item is returned
        assertTrue(foundItem.isPresent());
        assertEquals(itemId, foundItem.get().getId());
        assertEquals("Test Item", foundItem.get().getName());

        // Verify that the findById method was called
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void testGetItemById_ItemNotFound_ShouldReturnEmpty() {
        // Generate a random UUID for the item
        UUID itemId = UUID.randomUUID();

        // Mock the repository to return an empty result when searching by ID
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Execute the method
        Optional<Item> foundItem = Optional.ofNullable(itemService.getItemById(itemId));

        // Check if no item is found
        assertFalse(foundItem.isPresent());

        // Verify that the findById method was called
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void testDeleteItem_ShouldDeleteItem() {
        // Generate a random UUID for the item
        UUID itemId = UUID.randomUUID();

        // Mock the repository to do nothing when deleting
        doNothing().when(itemRepository).deleteById(itemId);

        // Execute the method
        itemService.deleteItem(itemId);

        // Verify that the deleteById method was called
        verify(itemRepository, times(1)).deleteById(itemId);
    }
}
