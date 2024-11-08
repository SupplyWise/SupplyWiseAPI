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
    private static final int MIN_BAR_CODE_LENGTH = 3;
    private static final int MAX_BAR_CODE_LENGTH = 30;

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item createItem(Item item) {
        if (!isItemValid(item)) {
            throw new IllegalArgumentException("Item is not valid");
        }
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(UUID id) {
        return itemRepository.findById(id).orElse(null);
    }

    public Item getItemByBarCode(int barCode) {
        if (!isBarCodeValid(barCode)) {
            throw new IllegalArgumentException("Bar code is not valid");
        }
        return itemRepository.findByBarCode(barCode).orElse(null);
    }

    public Item updateItem(UUID id, Item itemDetails) {
        Item item = itemRepository.findById(id).orElse(null);

        if (!isItemValid(itemDetails)) {
            throw new IllegalArgumentException("Item is not valid");
        }

        item.setName(itemDetails.getName());
        item.setBarCode(itemDetails.getBarCode());
        item.setCategory(itemDetails.getCategory());
        return itemRepository.save(item);
    }

    public void deleteItem(UUID id) {
        itemRepository.deleteById(id);
    }

    /* Helper functions */

    private boolean isBarCodeValid(int barCode) {
        return barCode >= MIN_BAR_CODE_LENGTH && barCode <= MAX_BAR_CODE_LENGTH;
    }

    private boolean isItemValid(Item item) {
        if (item == null) {
            return false;
        }

        String itemName = item.getName();
        int itemBarCode = item.getBarCode();
        Category itemCategory = item.getCategory();

        if (itemName == null || itemName.isEmpty() || itemName.length() < MIN_ITEM_NAME_LENGTH || itemName.length() > MAX_ITEM_NAME_LENGTH) {
            return false;
        }
        if (itemCategory == null || !EnumSet.allOf(Category.class).contains(itemCategory)) {
            return false;
        }

        return isBarCodeValid(itemBarCode);
    }
}
