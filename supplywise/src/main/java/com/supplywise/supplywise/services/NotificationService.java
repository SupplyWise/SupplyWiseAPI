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

    public Notification updateNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getActiveNotifications(UUID restaurantId) {
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
}
