package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.Category;
import com.supplywise.supplywise.repositories.ItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

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
    void testGetAllItems_ShouldReturnAllItems() {
        // Create a list of items
        List<Item> items = List.of(new Item(), new Item());

        // Mock the repository to return the list of items
        when(itemRepository.findAll()).thenReturn(items);

        // Execute the method
        List<Item> allItems = itemService.getAllItems();

        // Check if the list returned matches the mock
        assertEquals(2, allItems.size());

        // Verify that the findAll method was called
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetItemByBarCode_ItemFound_ShouldReturnItem() {
        // Item data
        int barCode = 123456;
        Item item = new Item();
        item.setBarCode(String.valueOf(barCode));
        item.setName("Test Item");

        // Mock the repository to return the item when searched by barcode
        when(itemRepository.findByBarCode(barCode)).thenReturn(Optional.of(item));

        // Execute the method
        Item foundItem = itemService.getItemByBarCode(barCode);

        // Check if the item is returned
        assertNotNull(foundItem);
        assertEquals(barCode, Integer.parseInt(foundItem.getBarCode()));
        assertEquals("Test Item", foundItem.getName());

        // Verify that the findByBarCode method was called
        verify(itemRepository, times(1)).findByBarCode(barCode);
    }

    @Test
    void testGetItemByBarCode_ItemNotFound_ShouldReturnNull() {
        // Item data
        int barCode = 123456;

        // Mock the repository to return an empty result when searching by barcode
        when(itemRepository.findByBarCode(barCode)).thenReturn(Optional.empty());

        // Execute the method
        Item foundItem = itemService.getItemByBarCode(barCode);

        // Check if no item is found
        assertNull(foundItem);

        // Verify that the findByBarCode method was called
        verify(itemRepository, times(1)).findByBarCode(barCode);
    }

    @Test
    void testUpdateItem_ItemFound_ShouldUpdateAndReturnItem() {
        // Generate a random UUID for the item
        UUID itemId = UUID.randomUUID();
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old Item");
        existingItem.setBarCode("123456");
        existingItem.setCategory(Category.EATABLE);

        Item updatedItemDetails = new Item();
        updatedItemDetails.setName("Updated Item");
        updatedItemDetails.setBarCode("654321");
        updatedItemDetails.setCategory(Category.DRINKABLE);

        // Mock the repository to return the existing item when searched by ID
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        // Mock the repository to return the updated item when saved
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItemDetails);

        // Execute the method
        Item updatedItem = itemService.updateItem(itemId, updatedItemDetails);

        // Check if the item is updated
        assertNotNull(updatedItem);
        assertEquals("Updated Item", updatedItem.getName());
        assertEquals("654321", updatedItem.getBarCode());
        assertEquals(Category.DRINKABLE, updatedItem.getCategory());

        // Verify that the findById and save methods were called
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testUpdateItem_ItemNotFound_ShouldReturnNull() {
        // Generate a random UUID for the item
        UUID itemId = UUID.randomUUID();
        Item updatedItemDetails = new Item();
        updatedItemDetails.setName("Updated Item");
        updatedItemDetails.setBarCode("654321");
        updatedItemDetails.setCategory(Category.EATABLE);

        // Mock the repository to return an empty result when searching by ID
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Execute the method
        Item updatedItem = itemService.updateItem(itemId, updatedItemDetails);

        // Check if no item is updated
        assertNull(updatedItem);

        // Verify that the findById method was called
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(0)).save(any(Item.class));
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
