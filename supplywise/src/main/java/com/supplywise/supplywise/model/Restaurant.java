package com.supplywise.supplywise.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
@Data
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_periodicity")
    private InventoryPeriodicity periodicity;

    @Column(name = "custom_inventory_periodicity")
    private Integer customInventoryPeriodicity; // Number of days for CUSTOM periodicity

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Restaurant(String name, Company company, InventoryPeriodicity periodicity, Integer customInventoryPeriodicity) {
        this.name = name;
        this.company = company;
        this.periodicity = periodicity;
        this.customInventoryPeriodicity = customInventoryPeriodicity;
    }

    public Restaurant(String name, Company company) {
        this.name = name;
        this.company = company;
    }
}
