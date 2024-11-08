package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @NotBlank(message = "Item must have a name")
    @Column(name = "name", nullable = false, length = 255)
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Column(name = "barcode", unique = true)
    @Pattern(regexp = "\\d{3,30}", message = "Bar code must be a numeric string with length between 3 and 30")
    private int barCode;

    @NotBlank(message = "Item must have a category")
    @Enumerated(EnumType.STRING)
    private Category category;

    public Item(String name, int barCode, Category category) {
        this.name = name;
        this.barCode = barCode;
        this.category = category;
    }
}