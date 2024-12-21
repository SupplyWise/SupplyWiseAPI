package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "item_stock")
@Data
@NoArgsConstructor
public class ItemStock {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "minimum_quantity", nullable = false)
    private int minimumQuantity = 0;

    @Column(name = "low_stock", nullable = false)
    private boolean lowStock = false;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_properties_id", nullable = false)
    private ItemProperties itemProperties;

    public ItemStock(int quantity, ItemProperties itemProperties) {
        this.quantity = quantity;
        this.minimumQuantity = 0;
        this.itemProperties = itemProperties;
        this.updateLowStockStatus();
    }

    public ItemStock(int quantity, int minimumQuantity, ItemProperties itemProperties) {
        this.quantity = quantity;
        this.minimumQuantity = minimumQuantity;
        this.itemProperties = itemProperties;
        this.updateLowStockStatus();
    }

    private void updateLowStockStatus() {
        this.lowStock = this.quantity < this.minimumQuantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateLowStockStatus();
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
        updateLowStockStatus();
    }

}