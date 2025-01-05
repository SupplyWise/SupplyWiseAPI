package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByRestaurantIdAndMessageContaining(UUID restaurantId, String itemName);
    List<Notification> findByRestaurantIdAndIsResolved(UUID restaurantId, boolean isResolved);
    List<Notification> findByRestaurantIdAndIsReminder(UUID restaurantId, boolean isReminder);
}
