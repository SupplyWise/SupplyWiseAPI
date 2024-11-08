package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
import com.supplywise.supplywise.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemPropertiesService {

    private static final int MIN_ITEM_QUANTITY = 1;

    private final ItemPropertiesRepository itemPropertiesRepository;
    private final ItemRepository itemRepository;

    public ItemPropertiesService(ItemPropertiesRepository itemPropertiesRepository, ItemRepository itemRepository) {
        this.itemPropertiesRepository = itemPropertiesRepository;
        this.itemRepository = itemRepository;
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

    /* Helper functions */

    public boolean isItemSetValid(ItemProperties itemSet) {
        Item item = itemRepository.findById(itemSet.getItem().getId()).orElse(null);
        if (item == null) {
            return false;
        }
        if (itemSet.getQuantity() < MIN_ITEM_QUANTITY) {
            return false;
        }
        if (itemSet.getExpirationDate() == null) {
            return false;
        }
        return true;
    }
}