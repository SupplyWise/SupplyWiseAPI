package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_properties")
@Data
@NoArgsConstructor
public class ItemProperties {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public ItemProperties(Item item, LocalDate expirationDate, Integer quantity) {
        this.item = item;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }
}
