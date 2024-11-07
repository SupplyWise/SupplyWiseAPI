package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Optional<Inventory> getInventoryById(UUID id) {
        return inventoryRepository.findById(id);
    }

    public List<Inventory> getInventoriesByRestaurant(Restaurant restaurant) {
        return inventoryRepository.findByRestaurant(restaurant);
    }

    public void deleteInventoryById(UUID id) {
        if (inventoryRepository.findById(id).isPresent()) {
            inventoryRepository.deleteById(id);
        }
    }
}
