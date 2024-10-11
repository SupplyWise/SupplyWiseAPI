package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User{

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Username is mandatory")
    @Size(min = 5, max = 255, message = "Username should be between 5 and 255 characters long")
    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Size(max = 255)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.MANAGER;   // default role is Manager

    @ManyToOne(fetch = FetchType.EAGER)
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