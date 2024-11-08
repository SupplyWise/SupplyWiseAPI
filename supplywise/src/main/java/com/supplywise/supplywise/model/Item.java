package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "barcode", unique = true)
    private String barCode;

    @Enumerated(EnumType.STRING)
    private Category category;

    
    public Item(String name, String barCode, Category category) {
        this.name = name;
        this.barCode = barCode;
        this.category = category;
    }
}