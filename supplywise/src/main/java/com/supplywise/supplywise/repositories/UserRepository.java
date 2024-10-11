package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContaining(String usernamePart);
    List<User> findByRole(Role role);
    List<User> findByRestaurantId(UUID restaurantId);
    List<User> findByRoleAndRestaurantId(Role role, UUID restaurantId);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsById(UUID id);
    Page<User> findAll(Pageable pageable);
}

