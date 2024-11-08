package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(UUID id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item updateItem(UUID id, Item itemDetails) {
        return itemRepository.findById(id).map(item -> {
            item.setName(itemDetails.getName());
            item.setBarCode(itemDetails.getBarCode());
            item.setCategory(itemDetails.getCategory());
            return itemRepository.save(item);
        }).orElse(null);
    }

    public void deleteItem(UUID id) {
        itemRepository.deleteById(id);
    }
}
