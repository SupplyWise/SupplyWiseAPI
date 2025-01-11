package com.supplywise.supplywise.services;

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
import java.time.format.DateTimeFormatter;

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

    private static final String NOTIFICATION_NOT_FOUND = "Notification not found";
    private static final String NOTIFICATION_ID_NOT_FOUND = "Notification with ID {} not found";

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
        logger.info("Creating notification: {}", notification.getMessage());

        // Check if the notification already exists by its text
        if (notification.isReminder()) {
            logger.info("Checking for existing reminders for restaurant ID: {}", notification.getRestaurant().getId());
            List<Notification> existingReminders = notificationRepository.findByRestaurantIdAndIsReminder(
                    notification.getRestaurant().getId(), true
            );

            for (Notification reminder : existingReminders) {
                if (reminder.getMessage().equals(notification.getMessage())) {
                    logger.info("Notification already exists: {}", notification.getMessage());
                    return reminder;
                }
            }
        }

        logger.info("Saving notification to database: {}", notification.getMessage());
        Notification savedNotification = notificationRepository.save(notification);

        logger.info("Broadcasting notification via WebSocket: {}", notification.getMessage());
        notificationWebSocketHandler.sendNotification(String.format("Notification: %s", notification.getMessage()));

        return savedNotification;
    }

    public Notification updateNotification(Notification notification) {
        logger.info("Updating notification with ID: {}", notification.getId());
        return notificationRepository.save(notification);
    }

    public List<Notification> getActiveNotifications(UUID restaurantId) {
        logger.info("Fetching active notifications for restaurant ID: {}", restaurantId);
        checkForInventoryCount(restaurantId);
        return notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false);
    }

    public void resolveNotification(UUID notificationId) {
        logger.info("Resolving notification with ID: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error(NOTIFICATION_ID_NOT_FOUND, notificationId);
                    return new IllegalArgumentException(NOTIFICATION_NOT_FOUND);
                });

        notification.setResolved(true);
        notificationRepository.save(notification);
        logger.info("Notification with ID {} resolved", notificationId);
    }

    public void readNotification(UUID notificationId) {
        logger.info("Marking notification with ID {} as read", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error(NOTIFICATION_ID_NOT_FOUND, notificationId);
                    return new IllegalArgumentException(NOTIFICATION_NOT_FOUND);
                });

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void unreadNotification(UUID notificationId) {
        logger.info("Marking notification with ID {} as unread", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error(NOTIFICATION_ID_NOT_FOUND, notificationId);
                    return new IllegalArgumentException(NOTIFICATION_NOT_FOUND);
                });

        notification.markUnread();
        notificationRepository.save(notification);
    }

    public void deleteNotification(Notification notification) {
        logger.info("Deleting notification with ID: {}", notification.getId());
        notificationRepository.delete(notification);
    }

    public void checkForInventoryCount(UUID restaurantId) {
        logger.info("Checking inventory count for restaurant ID: {}", restaurantId);
        Optional<Restaurant> opRestaurant = restaurantRepository.findById(restaurantId);

        if (opRestaurant.isPresent()) {
            Restaurant restaurant = opRestaurant.get();
            List<Inventory> inventories = inventoryRepository.findByRestaurant(restaurant);

            for (Inventory inventory : inventories) {
                if (inventory != null && inventory.getClosingDate() == null) {
                    logger.info("Inventory without closing date found for restaurant ID: {}", restaurantId);
                    createInventoryCountReminder(
                        restaurantId,
                        restaurant.getName(),
                        inventory.getExpectedClosingDate()
                    );
                }
            }
        } else {
            logger.warn("Restaurant with ID {} not found", restaurantId);
        }
    }

    public void createInventoryCountReminder(UUID restaurantId, String restaurantName, LocalDateTime closingDate) {
        if (closingDate.isBefore(LocalDateTime.now())) {
            logger.info("Creating inventory count reminder for restaurant ID: {}", restaurantId);

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> {
                        logger.error("Restaurant with ID {} not found", restaurantId);
                        return new IllegalArgumentException("Restaurant not found");
                    });

            String formattedDate = closingDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

            String message = String.format(
                    "Reminder: It is time to perform the inventory count for %s. Scheduled date: %s.",
                    restaurantName,
                    formattedDate
            );

            Notification notification = new Notification(restaurant, message);
            notification.markAsReminder(); // Mark as reminder to prevent multiple reminders for the same inventory
            createNotification(notification); // Save and broadcast
        } else {
            logger.info("Closing date {} is not before the current time. No reminder created.", closingDate);
        }
    }

    public void clearRemindersByRestaurant(UUID restaurantId) {
        logger.info("Clearing reminders for restaurant ID: {}", restaurantId);
        List<Notification> reminders = notificationRepository.findByRestaurantIdAndIsReminder(restaurantId, true);

        for (Notification reminder : reminders) {
            reminder.setResolved(true);
            notificationRepository.save(reminder);
        }
    }
}
