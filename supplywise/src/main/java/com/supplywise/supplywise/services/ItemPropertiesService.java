package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
import com.supplywise.supplywise.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemPropertiesService {

    private static final int MIN_ITEM_QUANTITY = 1;

    private final ItemPropertiesRepository itemPropertiesRepository;
    private final ItemRepository itemRepository;

    public ItemPropertiesService(ItemPropertiesRepository itemPropertiesRepository, ItemRepository itemRepository) {
        this.itemPropertiesRepository = itemPropertiesRepository;
        this.itemRepository = itemRepository;
    }

    public ItemProperties createItemProperties(ItemProperties itemProperties) {
        if (!isItemPropertiesValid(itemProperties)) {
            throw new IllegalArgumentException("Item properties are not valid.");
        }
        return itemPropertiesRepository.save(itemProperties);
    }

    public List<ItemProperties> getAllItemProperties() {
        return itemPropertiesRepository.findAll();
    }

    public ItemProperties getItemPropertiesById(UUID id) {
        return itemPropertiesRepository.findById(id).orElse(null);
    }

    public void deleteItemProperties(UUID id) {
        itemPropertiesRepository.deleteById(id);
    }

    // Method to update general ItemProperties and handle role-based logic for minimum stock
    public ItemProperties updateItemProperties(UUID id, ItemProperties newItemProperties, boolean canEditMinimumStock) {
        ItemProperties existingItemProperties = itemPropertiesRepository.findById(id).orElse(null);

        if (existingItemProperties == null) {
            return null;
        }

        // Update general fields
        if (newItemProperties.getItem() != null) {
            existingItemProperties.setItem(newItemProperties.getItem());
        }
        if (newItemProperties.getExpirationDate() != null) {
            existingItemProperties.setExpirationDate(newItemProperties.getExpirationDate());
        }
        if (newItemProperties.getQuantity() != null) {
            existingItemProperties.setQuantity(newItemProperties.getQuantity());
        }

        // Update minimum stock only if the user has the necessary permissions
        if (canEditMinimumStock && newItemProperties.getMinimumStockQuantity() != null) {
            if (newItemProperties.getMinimumStockQuantity() < 0) {
                throw new IllegalArgumentException("Minimum stock quantity cannot be negative");
            }
            existingItemProperties.setMinimumStockQuantity(newItemProperties.getMinimumStockQuantity());
        }

        // Validate the updated item properties
        if (!isItemPropertiesValid(existingItemProperties)) {
            throw new IllegalArgumentException("Updated item properties are not valid.");
        }

        return itemPropertiesRepository.save(existingItemProperties);
    }

    // Method to update only the minimum stock quantity (for authorized roles)
    public ItemProperties updateMinimumStockQuantity(UUID id, Integer minimumStock) {
        if (minimumStock < 0) {
            throw new IllegalArgumentException("Minimum stock quantity cannot be negative");
        }

        ItemProperties itemProperties = itemPropertiesRepository.findById(id)
                .orElse(null);

        if (itemProperties == null) {
            return null;
        }

        itemProperties.setMinimumStockQuantity(minimumStock);

        return itemPropertiesRepository.save(itemProperties);
    }

    /* Helper functions */

    private boolean isItemPropertiesValid(ItemProperties itemProperties) {
        if (itemProperties == null) {
            return false;
        }

        Item item = itemRepository.findById(itemProperties.getItem().getId()).orElse(null);
        return item != null && itemProperties.getQuantity() >= MIN_ITEM_QUANTITY && itemProperties.getExpirationDate() != null;
    }
}
