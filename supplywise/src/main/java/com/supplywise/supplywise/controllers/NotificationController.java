package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.services.NotificationService;
import com.supplywise.supplywise.services.AuthHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Get notifications for the authenticated restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications fetched successfully")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @GetMapping
    public List<Notification> getNotifications() {
        String restaurantId = authHandler.getAuthenticatedRestaurantId();
        logger.info("Fetching notifications for restaurant: {}", restaurantId);
        return notificationService.getActiveNotifications(UUID.fromString(restaurantId));
    }

    @Operation(summary = "Get notifications for a specific restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Restaurant ID not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @GetMapping("/{restaurantId}")
    public List<Notification> getNotificationsForRestaurant(@PathVariable UUID restaurantId) {
        logger.info("Fetching notifications for restaurant with ID: {}", restaurantId);
        return notificationService.getActiveNotifications(restaurantId);
    }

    @Operation(summary = "Resolve a notification by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Notification ID not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @PostMapping("/{notificationId}/resolve")
    public void resolveNotification(@PathVariable UUID notificationId) {
        notificationService.resolveNotification(notificationId);
    }

    @Operation(summary = "Mark a notification as read by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification ID not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @PostMapping("/{notificationId}/read")
    public void readNotification(@PathVariable UUID notificationId) {
        notificationService.readNotification(notificationId);
    }

    @Operation(summary = "Mark a notification as unread by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification marked as unread successfully"),
            @ApiResponse(responseCode = "404", description = "Notification ID not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FRANCHISE_OWNER', 'ROLE_MANAGER', 'ROLE_MANAGER_MASTER')")
    @PostMapping("/{notificationId}/unread")
    public void unreadNotification(@PathVariable UUID notificationId) {
        notificationService.unreadNotification(notificationId);
    }
}
