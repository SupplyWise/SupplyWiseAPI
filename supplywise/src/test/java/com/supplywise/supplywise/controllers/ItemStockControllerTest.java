package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.ItemStock;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemStockControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemStockService itemStockService;

    @InjectMocks
    private ItemStockController itemStockController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemStockController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateItemStock_Success() throws Exception {
        UUID itemPropertiesId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        itemProperties.setId(itemPropertiesId);

        ItemStock itemStock = new ItemStock(100, itemProperties);

        when(itemStockService.saveItemStock(any(ItemStock.class))).thenReturn(itemStock);

        mockMvc.perform(post("/api/item-stock/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemStock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(100));

        verify(itemStockService, times(1)).saveItemStock(any(ItemStock.class));
    }

    @Test
    void testCreateItemStock_InvalidItemProperties() throws Exception {
        ItemStock itemStock = new ItemStock();
        itemStock.setQuantity(100);
        itemStock.setItemProperties(null);

        mockMvc.perform(post("/api/item-stock/")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(itemStock)))
            .andExpect(status().isBadRequest());

        verify(itemStockService, never()).saveItemStock(any(ItemStock.class));
    }

    @Test
    void testGetItemStockById_Success() throws Exception {
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, itemProperties);
        itemStock.setId(itemStockId);

        when(itemStockService.getItemStockById(any(UUID.class))).thenReturn(Optional.of(itemStock));

        mockMvc.perform(get("/api/item-stock/" + itemStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));

        verify(itemStockService, times(1)).getItemStockById(any(UUID.class));
    }

    @Test
    void testGetItemStockById_NotFound() throws Exception {
        UUID itemStockId = UUID.randomUUID();
        when(itemStockService.getItemStockById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/item-stock/" + itemStockId))
                .andExpect(status().isNotFound());

        verify(itemStockService, times(1)).getItemStockById(any(UUID.class));
    }

    @Test
    void testUpdateItemStock_Success() throws Exception {
        UUID itemStockId = UUID.randomUUID();
        ItemProperties itemProperties = new ItemProperties();
        ItemStock itemStock = new ItemStock(100, itemProperties);
        itemStock.setId(itemStockId);

        when(itemStockService.updateItemStockQuantity(any(UUID.class), anyInt())).thenReturn(Optional.of(itemStock));

        mockMvc.perform(put("/api/item-stock/" + itemStockId)
                        .param("newQuantity", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));

        verify(itemStockService, times(1)).updateItemStockQuantity(any(UUID.class), anyInt());
    }

    @Test
    void testUpdateItemStock_NotFound() throws Exception {
        UUID itemStockId = UUID.randomUUID();

        when(itemStockService.updateItemStockQuantity(any(UUID.class), anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/item-stock/" + itemStockId)
                        .param("newQuantity", "200"))
                .andExpect(status().isNotFound());

        verify(itemStockService, times(1)).updateItemStockQuantity(any(UUID.class), anyInt());
    }

    @Test
    void testDeleteItemStockById_Success() throws Exception {
        UUID itemStockId = UUID.randomUUID();
        when(itemStockService.itemStockExistsById(any(UUID.class))).thenReturn(true);

        mockMvc.perform(delete("/api/item-stock/" + itemStockId))
                .andExpect(status().isOk());

        verify(itemStockService, times(1)).deleteItemStockById(any(UUID.class));
    }

    @Test
    void testDeleteItemStockById_NotFound() throws Exception {
        UUID itemStockId = UUID.randomUUID();
        when(itemStockService.itemStockExistsById(any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete("/api/item-stock/" + itemStockId))
                .andExpect(status().isNotFound());

        verify(itemStockService, times(1)).itemStockExistsById(any(UUID.class));
    }

}
