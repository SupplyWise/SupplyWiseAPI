package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
import com.supplywise.supplywise.repositories.ItemRepository;
import com.supplywise.supplywise.repositories.InventoryRepository;
import com.supplywise.supplywise.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemPropertiesService {

    private static final int MIN_ITEM_QUANTITY = 1;

    private final ItemPropertiesRepository itemPropertiesRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public ItemPropertiesService(ItemPropertiesRepository itemPropertiesRepository, 
                                 ItemRepository itemRepository, 
                                 InventoryRepository inventoryRepository, 
                                 NotificationRepository notificationRepository,
                                 NotificationService notificationService) {
        this.itemPropertiesRepository = itemPropertiesRepository;
        this.itemRepository = itemRepository;
        this.inventoryRepository = inventoryRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
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

    public ItemProperties updateItemProperties(UUID id, ItemProperties newItemProperties) {
        ItemProperties existingItemProperties = itemPropertiesRepository.findById(id).orElse(null);

        if (existingItemProperties == null) {
            return null;
        }

        // Update fields
        if (newItemProperties.getItem() != null) {
            existingItemProperties.setItem(newItemProperties.getItem());
        }
        if (newItemProperties.getExpirationDate() != null) {
            existingItemProperties.setExpirationDate(newItemProperties.getExpirationDate());
        }
        if (newItemProperties.getQuantity() != null) {
            existingItemProperties.setQuantity(newItemProperties.getQuantity());
        }
        if (newItemProperties.getMinimumStockQuantity() != null) {
            if (newItemProperties.getMinimumStockQuantity() < 0) {
                throw new IllegalArgumentException("Minimum stock quantity cannot be negative");
            }
            existingItemProperties.setMinimumStockQuantity(newItemProperties.getMinimumStockQuantity());
        }

        if (!isItemPropertiesValid(existingItemProperties)) {
            throw new IllegalArgumentException("Updated item properties are not valid.");
        }

        // Notification logic
        handleStockNotifications(existingItemProperties);

        return itemPropertiesRepository.save(existingItemProperties);
    }

    private void handleStockNotifications(ItemProperties itemProperties) {
        Inventory inventory = inventoryRepository.findInventoryByItemPropertiesId(itemProperties.getId())
                .orElseThrow(() -> new IllegalArgumentException("Associated Inventory not found"));

        Optional<Notification> existingNotification = notificationRepository.findByRestaurantIdAndMessageContaining(
                inventory.getRestaurant().getId(),
                itemProperties.getItem().getName()
        );

        if (itemProperties.getQuantity() >= itemProperties.getMinimumStockQuantity()) {
            // Remove notification if quantity >= minimum stock
            existingNotification.ifPresent(notificationService::deleteNotification);
        } else {
            // Create notification if quantity < minimum stock and no existing notification
            if (existingNotification.isEmpty()) {
                String message = String.format("Item '%s' is below minimum stock in restaurant '%s'.",
                        itemProperties.getItem().getName(),
                        inventory.getRestaurant().getName());

                Notification notification = new Notification(inventory.getRestaurant(), message);
                notificationService.createNotification(notification);
            }
        }
    }

    // Method to update only the minimum stock quantity (for authorized roles)
    public ItemProperties updateMinimumStockQuantity(UUID id, Integer minimumStock) {
        if (minimumStock < 0) {
            throw new IllegalArgumentException("Minimum stock quantity cannot be negative");
        }

        ItemProperties itemProperties = itemPropertiesRepository.findById(id).orElse(null);

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
