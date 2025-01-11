package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByRestaurant(Restaurant restaurant);

    @Query("SELECT i FROM Inventory i JOIN i.items ip WHERE ip.id = :itemPropertiesId")
    Optional<Inventory> findInventoryByItemPropertiesId(@Param("itemPropertiesId") UUID itemPropertiesId);
}
