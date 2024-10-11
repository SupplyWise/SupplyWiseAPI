package com.supplywise.supplywise.model;

import jakarta.persistence.*;

import lombok.Getter;   // to automatically generate getter methods for all fields
import lombok.Setter;   // to automatically generate setter methods for all fields
import lombok.NoArgsConstructor; // to automatically generate no-args constructor
import lombok.AllArgsConstructor;   // to automatically generate all args constructor

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User{

    @Id
    @GeneratedValue()
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.MANAGER;   // default role is Manager

    @ManyToOne(fetch = FetchType.LAZY)  // to the Restaurant entity not be fetched from the database until it is accessed
    @JoinColumn(name = "restaurant_id", nullable = true)
    private Restaurant restaurant;

    @Column(name = "created_at", nullable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

    
    public User(String username, String password, Role role, Restaurant restaurant){
        this.username = username;
        this.password = password;
        this.role = role;
        this.restaurant = restaurant;
    }
}