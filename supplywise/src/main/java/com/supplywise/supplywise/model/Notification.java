package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved = false;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "is_reminder", nullable = false)
    private boolean isReminder = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(Restaurant restaurant, String message) {
        this.restaurant = restaurant;
        this.message = message;
    }

    public void markResolved() {
        this.isResolved = true;
    }

    public void markRead() {
        this.isRead = true;
    }

    public void markUnread() {
        this.isRead = false;
    }

    public void markAsReminder() {
        this.isReminder = true;
    }
}
