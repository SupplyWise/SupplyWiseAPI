package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRestaurantIdAndIsResolved(UUID restaurantId, boolean isResolved);
}
