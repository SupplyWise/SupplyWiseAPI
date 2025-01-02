package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.repositories.NotificationRepository;
import com.supplywise.supplywise.websocket.NotificationWebSocketHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationWebSocketHandler notificationWebSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    public Notification createNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // Broadcast the notification via WebSocket
        String message = String.format("Notification: %s", notification.getMessage());
        notificationWebSocketHandler.sendNotification(message);

        return savedNotification;
    }

    public List<Notification> getActiveNotifications(UUID restaurantId) {
        return notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false);
    }

    public void resolveNotificationsForRestaurant(UUID restaurantId) {
        List<Notification> notifications = notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false);
        notifications.forEach(Notification::markResolved);
        notificationRepository.saveAll(notifications);
    }
}
