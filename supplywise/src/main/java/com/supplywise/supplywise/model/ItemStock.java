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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_properties_id", nullable = false)
    private ItemProperties itemProperties;

    public ItemStock(int quantity, ItemProperties itemProperties) {
        this.quantity = quantity;
        this.itemProperties = itemProperties;
    }

}