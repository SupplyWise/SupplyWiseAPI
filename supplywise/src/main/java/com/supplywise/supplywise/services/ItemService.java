package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Category;
import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.repositories.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private static final int MIN_ITEM_NAME_LENGTH = 3;
    private static final int MAX_ITEM_NAME_LENGTH = 100;
    private static final String ITEM_BAR_CODE_REGEX = "\\d{3,30}"; //List of integers with lengths 3 to 30 digits

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

    public Item getItemByBarCode(int barCode) {
        return itemRepository.findByBarCode(barCode).orElse(null);
    }

    public Item updateItem(UUID id, Item itemDetails) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setName(itemDetails.getName());
            item.setBarCode(itemDetails.getBarCode());
            item.setCategory(itemDetails.getCategory());
            return itemRepository.save(item);
        }
        return null;
    }

    public void deleteItem(UUID id) {
        itemRepository.deleteById(id);
    }

    /* Helper functions */

    public boolean isItemValid(Item item) {
        String itemName = item.getName();
        String itemBarCode = item.getBarCode();
        Category itemCategory = item.getCategory();

        if (itemName == null || itemName.isEmpty() || itemName.length() < MIN_ITEM_NAME_LENGTH || itemName.length() > MAX_ITEM_NAME_LENGTH) {
            return false;
        }
        if (itemBarCode == null || !itemBarCode.matches(ITEM_BAR_CODE_REGEX)) {
            return false;
        }
        if (itemCategory == null || !EnumSet.allOf(Category.class).contains(itemCategory)) {
            return false;
        }
        return true;
    }
}
