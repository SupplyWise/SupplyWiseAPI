package com.supplywise.supplywise.repositories;

import com.supplywise.supplywise.model.ItemProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemPropertiesRepository extends JpaRepository<ItemProperties, UUID> {
}
