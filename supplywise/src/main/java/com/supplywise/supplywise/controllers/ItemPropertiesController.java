package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemPropertiesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-properties")
public class ItemPropertiesController {

    private final ItemPropertiesService itemPropertiesService;

    public ItemPropertiesController(ItemPropertiesService itemPropertiesService) {
        this.itemPropertiesService = itemPropertiesService;
    }

    @PostMapping("/create")
    public ResponseEntity<ItemProperties> createItemProperties(@RequestBody ItemProperties itemProperties) {
        return new ResponseEntity<>(itemPropertiesService.createItemProperties(itemProperties), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemProperties>> getAllItemProperties() {
        return ResponseEntity.ok(itemPropertiesService.getAllItemProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemProperties> getItemPropertiesById(@PathVariable UUID id) {
        ItemProperties itemProperties = itemPropertiesService.getItemPropertiesById(id);
        return itemProperties != null ? ResponseEntity.ok(itemProperties) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemProperties(@PathVariable UUID id) {
        itemPropertiesService.deleteItemProperties(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemProperties> updateItemProperties(@PathVariable UUID id, @RequestBody ItemProperties itemProperties) {
        ItemProperties updatedItemProperties = itemPropertiesService.updateItemProperties(id, itemProperties);
        return updatedItemProperties != null ? ResponseEntity.ok(updatedItemProperties) : ResponseEntity.notFound().build();
    }
}
