package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemPropertiesService {

    private final ItemPropertiesRepository itemPropertiesRepository;

    public ItemPropertiesService(ItemPropertiesRepository itemPropertiesRepository) {
        this.itemPropertiesRepository = itemPropertiesRepository;
    }

    public ItemProperties createItemProperties(ItemProperties itemProperties) {
        return itemPropertiesRepository.save(itemProperties);
    }

    public List<ItemProperties> getAllItemProperties() {
        return itemPropertiesRepository.findAll();
    }

    public ItemProperties getItemPropertiesById(UUID id) {
        return itemPropertiesRepository.findById(id).orElse(null);
    }

    public void deleteItemProperties(UUID id) {
        itemPropertiesRepository.deleteById(id);
    }

    public ItemProperties updateItemProperties(UUID id, ItemProperties itemProperties) {
        ItemProperties itemPropertiesToUpdate = itemPropertiesRepository.findById(id).orElse(null);
        if (itemPropertiesToUpdate == null) {
            return null;
        }
        itemPropertiesToUpdate.setItem(itemProperties.getItem());
        itemPropertiesToUpdate.setExpirationDate(itemProperties.getExpirationDate());
        itemPropertiesToUpdate.setQuantity(itemProperties.getQuantity());
        return itemPropertiesRepository.save(itemPropertiesToUpdate);
    }
}
