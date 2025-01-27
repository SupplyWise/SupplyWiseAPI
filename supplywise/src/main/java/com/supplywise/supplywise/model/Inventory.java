package com.supplywise.supplywise.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "inventory")
    private Set<ItemProperties> items = new HashSet<>();

    @Column(name = "emission_date", nullable = false)
    private LocalDateTime emissionDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "expected_closing_date")
    private LocalDateTime expectedClosingDate;

    @Column(name = "report")
    private String report;

    @Column(name = "closed_by_user_id")
    private String closedByUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Inventory(Restaurant restaurant, LocalDateTime emissionDate, LocalDateTime expectedClosingDate) {
        this.restaurant = restaurant;
        this.emissionDate = emissionDate;
        this.expectedClosingDate = expectedClosingDate;
    }

    public Inventory(Restaurant restaurant, LocalDateTime emissionDate) {
        this.restaurant = restaurant;
        this.emissionDate = emissionDate;
        this.expectedClosingDate = null;
    }

    public void addItemProperties(ItemProperties item) {
        items.add(item);
    }

    public void removeItemProperties(ItemProperties item) {
        items.remove(item);
    }

    public void setClosedByUser(String user) {
        this.closedByUser = user;
    }

    public String getClosedByUser() {
        return closedByUser;
    }
}
