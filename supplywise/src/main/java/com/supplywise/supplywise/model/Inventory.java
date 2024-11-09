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
    @JoinColumn(name = "inventory_id")
    private Set<ItemStock> itemStocks = new HashSet<>();

    @Column(name = "emission_date", nullable = false)
    private LocalDateTime emissionDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "expected_closing_date")
    private LocalDateTime expectedClosingDate;

    @Column(name = "report", nullable = false)
    private String report;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Inventory(Restaurant restaurant, LocalDateTime emissionDate, LocalDateTime closingDate, LocalDateTime expectedClosingDate, String report) {
        this.restaurant = restaurant;
        this.emissionDate = emissionDate;
        this.closingDate = closingDate;
        this.expectedClosingDate = expectedClosingDate;
        this.report = report;
    }

    public void addItemStock(ItemStock itemStock) {
        itemStocks.add(itemStock);
    }

    public void removeItemStock(ItemStock itemStock) {
        itemStocks.remove(itemStock);
    }

}
