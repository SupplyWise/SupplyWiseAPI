package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.services.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{restaurantId}")
    public List<Notification> getNotifications(@PathVariable UUID restaurantId) {
        return notificationService.getActiveNotifications(restaurantId);
    }

    @PostMapping("/{notificationId}/resolve")
    public void resolveNotification(@PathVariable UUID notificationId) {
        notificationService.resolveNotification(notificationId);
    }
}
