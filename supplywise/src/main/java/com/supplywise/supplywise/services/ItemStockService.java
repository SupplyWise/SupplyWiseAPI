package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ItemStockService {

    private final ItemStockRepository itemStockRepository;

    @Autowired
    public ItemStockService(ItemStockRepository itemStockRepository) {
        this.itemStockRepository = itemStockRepository;
    }

    // Get an item stock by ID
    public Optional<ItemStock> getItemStockById(UUID id) {
        return itemStockRepository.findById(id);
    }

    // Create a new item stock
    public ItemStock saveItemStock(ItemStock itemStock) {
        ItemProperties itemProperties = itemStock.getItemProperties();
        if (itemProperties == null) {
            throw new IllegalArgumentException("Invalid or missing item properties");
        }
        return itemStockRepository.save(itemStock);
    }

    // Check if an item stock exists by ID
    public boolean itemStockExistsById(UUID id) {
        return itemStockRepository.existsById(id);
    }

    // Update an item stock's quantity (other fields stay the same)
    public Optional<ItemStock> updateItemStockQuantity(UUID id, int newQuantity) {
        Optional<ItemStock> itemStockOptional = itemStockRepository.findById(id);

        if (itemStockOptional.isPresent()) {
            ItemStock itemStock = itemStockOptional.get();
            itemStock.setQuantity(newQuantity);
            return Optional.of(itemStockRepository.save(itemStock));
        }

        return Optional.empty();
    }

    // Delete an item stock by ID
    public void deleteItemStockById(UUID id) {
        if (itemStockRepository.existsById(id)) {
            itemStockRepository.deleteById(id);
        }
    }

}