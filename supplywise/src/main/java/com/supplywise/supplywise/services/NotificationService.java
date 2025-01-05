package com.supplywise.supplywise.services;

import com.supplywise.supplywise.controllers.CompanyController;
import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.NotificationRepository;
import com.supplywise.supplywise.repositories.RestaurantRepository;
import com.supplywise.supplywise.repositories.InventoryRepository;
import com.supplywise.supplywise.websocket.NotificationWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final RestaurantRepository restaurantRepository;
    private final InventoryRepository inventoryRepository;

    
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationWebSocketHandler notificationWebSocketHandler,
                               RestaurantRepository restaurantRepository,
                               InventoryRepository inventoryRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.restaurantRepository = restaurantRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Notification createNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // Broadcast the notification via WebSocket
        String message = String.format("Notification: %s", notification.getMessage());
        notificationWebSocketHandler.sendNotification(message);

        return savedNotification;
    }

    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getActiveNotifications(UUID restaurantId) {
        checkForInventoryCount(restaurantId);
        return notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false);
    }

    public void resolveNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setResolved(true); // Mark as resolved
        notificationRepository.save(notification);
    }

    public void readNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setRead(true); // Mark as read
        notificationRepository.save(notification);
    }

    public void unreadNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.markUnread();
        notificationRepository.save(notification);
    }    

    public void deleteNotification(Notification notification) {
        notificationRepository.delete(notification);
    }    

    public void checkForInventoryCount(UUID restaurantId){
        Optional<Restaurant> opRestaurant = restaurantRepository.findById(restaurantId);   
        if (opRestaurant.isPresent()) {   
            Restaurant restaurant = opRestaurant.get();
            List<Inventory> inventories = inventoryRepository.findByRestaurant(restaurant);

            for (Inventory inventory : inventories) {
                if (inventory != null && inventory.getClosingDate() == null) {
                    createInventoryCountReminder(
                        restaurantId,
                        restaurant.getName(),
                        inventory.getExpectedClosingDate()
                    );
                }
            }
        }
    }

    public void createInventoryCountReminder(UUID restaurantId, String restaurantName, LocalDateTime closingDate) {
        if (closingDate.isBefore(LocalDateTime.now())) {
            String message = String.format(
                "Reminder: It is time to perform the inventory count for %s. Scheduled date: %s.",
                restaurantName,
                closingDate
            );
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            Notification notification = new Notification(restaurant, message);
            createNotification(notification); // Save and broadcast
        }
    }
}
