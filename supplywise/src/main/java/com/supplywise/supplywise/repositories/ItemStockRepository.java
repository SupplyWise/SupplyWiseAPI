package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.ItemStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemStockRepository extends JpaRepository<ItemStock, UUID> {
}
