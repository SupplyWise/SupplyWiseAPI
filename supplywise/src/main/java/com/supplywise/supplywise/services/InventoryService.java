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
    private final ItemStockRepository itemStockRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ItemStockRepository itemStockRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemStockRepository = itemStockRepository;
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
        return inventoryRepository.findById(id).map(existingInventory ->
            {
                existingInventory.setEmissionDate(inventoryDetails.getEmissionDate());
                existingInventory.setClosingDate(inventoryDetails.getClosingDate());
                existingInventory.setExpectedClosingDate(inventoryDetails.getExpectedClosingDate());
                existingInventory.setReport(inventoryDetails.getReport());
                existingInventory.setRestaurant(inventoryDetails.getRestaurant());

                existingInventory.getItemStocks().clear();
                for (ItemStock itemStock: inventoryDetails.getItemStocks()) {
                    existingInventory.addItemStock(itemStock);
                }

                return Optional.of(inventoryRepository.save(existingInventory));
            }
        ).orElse(Optional.empty());
    }

    public List<ItemStock> getItemStocksByInventoryId(UUID inventoryId) {
        Optional<Inventory> inventory = getInventoryById(inventoryId);
        return inventory.map(Inventory::getItemStocks).orElse(null);
    }

    // public ItemStock saveItemStock(ItemStock itemStock) {
    //     return itemStockRepository.save(itemStock);
    // }

    // public void deleteItemStockById(UUID id) {
    //     itemStockRepository.deleteById(id);
    // }

}
