package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.model.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email); // Intended when firing an employee
    List<User> findByRestaurantId(UUID restaurantId);
    Company findByCompanyId(UUID companyId);
    
    // Useful for admins only
    List<User> findByRole(Role role);
    Page<User> findAll(Pageable pageable);
}

