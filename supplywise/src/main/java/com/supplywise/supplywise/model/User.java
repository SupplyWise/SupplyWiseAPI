package com.supplywise.supplywise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User{

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Fullname is mandatory")
    @Size(min = 5, max = 255, message = "Fullname should be between 5 and 255 characters long")
    @Column(name = "fullname", nullable = false, length = 255)
    private String fullname;

    @NotBlank(message = "Email is mandatory")
    @Size(min = 5, max = 255, message = "Email should be between 5 and 255 characters long")
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 5, max = 255, message = "Password should be between 5 and 255 characters long")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.DISASSOCIATED;   // default role is Disassociated (can´t be manager because it needs a restaurant)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restaurant_id", nullable = true)
    private Restaurant restaurant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @Column(name = "created_at", nullable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public User(String fullname, String email, String password, Role role, Restaurant restaurant, Company company) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.restaurant = restaurant;
        this.company = company;
    }
}