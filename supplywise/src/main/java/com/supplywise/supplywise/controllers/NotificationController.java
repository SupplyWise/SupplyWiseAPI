package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.services.NotificationService;
import com.supplywise.supplywise.services.AuthHandler;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthHandler authHandler;
    private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationService notificationService, AuthHandler authHandler) {
        this.notificationService = notificationService;
        this.authHandler = authHandler;
    }

    @GetMapping
    public List<Notification> getNotifications() {
        String restaurantId = authHandler.getAuthenticatedRestaurantId();
        logger.info("Fetching notifications for restaurant: {}", restaurantId);
        return notificationService.getActiveNotifications(UUID.fromString(restaurantId));
    }

    @PostMapping("/{notificationId}/resolve")
    public void resolveNotification(@PathVariable UUID notificationId) {
        notificationService.resolveNotification(notificationId);
    }

    @PostMapping("/{notificationId}/read")
    public void readNotification(@PathVariable UUID notificationId) {
        notificationService.readNotification(notificationId);
    }
}
