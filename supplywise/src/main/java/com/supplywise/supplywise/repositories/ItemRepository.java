package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    Optional<Item> findByBarCode(int barCode);
}
