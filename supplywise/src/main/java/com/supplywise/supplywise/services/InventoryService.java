package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.ItemStock;
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

    public Optional<Inventory> updateInventory(UUID id, Inventory inventoryDetails) {
        return inventoryRepository.findById(id).map(existingInventory -> {
            // Update fields
            existingInventory.setEmissionDate(inventoryDetails.getEmissionDate());
            existingInventory.setClosingDate(inventoryDetails.getClosingDate());
            existingInventory.setExpectedClosingDate(inventoryDetails.getExpectedClosingDate());
            existingInventory.setReport(inventoryDetails.getReport());
            existingInventory.setRestaurant(inventoryDetails.getRestaurant());
    
            // Clear existing item stocks and re-add the new ones with inventory reference
            existingInventory.getItemStocks().clear();
            for (ItemStock itemStock : inventoryDetails.getItemStocks()) {
                existingInventory.addItemStock(itemStock);
            }
    
            return Optional.of(inventoryRepository.save(existingInventory));
        }).orElse(Optional.empty());
    }

}
