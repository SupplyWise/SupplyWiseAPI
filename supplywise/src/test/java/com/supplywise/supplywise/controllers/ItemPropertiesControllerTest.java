package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.ItemPropertiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemPropertiesController.class)
public class ItemPropertiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemPropertiesService itemPropertiesService;

    @MockBean
    private AuthHandler authHandler;

    private User authorizedUser;
    private User disassociatedUser;
    private ItemProperties itemProperties;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        itemProperties = new ItemProperties();
        itemProperties.setId(itemId);

        authorizedUser = new User();
        authorizedUser.setRole(Role.MANAGER); 

        disassociatedUser = new User();
        disassociatedUser.setRole(Role.DISASSOCIATED);
    }

    @Test
    void testCreateItemProperties_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemPropertiesService.createItemProperties(any(ItemProperties.class))).thenReturn(itemProperties);

        mockMvc.perform(post("/api/item-properties/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Item Property\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId.toString()));

        verify(itemPropertiesService, times(1)).createItemProperties(any(ItemProperties.class));
    }

    @Test
    void testCreateItemProperties_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(post("/api/item-properties/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Item Property\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to create item properties."));

        verify(itemPropertiesService, never()).createItemProperties(any(ItemProperties.class));
    }

    @Test
    void testGetAllItemProperties() throws Exception {
        when(itemPropertiesService.getAllItemProperties()).thenReturn(List.of(itemProperties));

        mockMvc.perform(get("/api/item-properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId.toString()));

        verify(itemPropertiesService, times(1)).getAllItemProperties();
    }

    @Test
    void testGetItemPropertiesById_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemPropertiesService.getItemPropertiesById(itemId)).thenReturn(itemProperties);

        mockMvc.perform(get("/api/item-properties/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()));

        verify(itemPropertiesService, times(1)).getItemPropertiesById(itemId);
    }

    @Test
    void testGetItemPropertiesById_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(get("/api/item-properties/{id}", itemId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to fetch item properties."));

        verify(itemPropertiesService, never()).getItemPropertiesById(itemId);
    }

    @Test
    void testGetItemPropertiesById_NotFound() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemPropertiesService.getItemPropertiesById(itemId)).thenReturn(null);

        mockMvc.perform(get("/api/item-properties/{id}", itemId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item Properties ID does not exist."));

        verify(itemPropertiesService, times(1)).getItemPropertiesById(itemId);
    }

    @Test
    void testDeleteItemProperties_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);

        mockMvc.perform(delete("/api/item-properties/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(itemPropertiesService, times(1)).deleteItemProperties(itemId);
    }

    @Test
    void testDeleteItemProperties_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(delete("/api/item-properties/{id}", itemId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to delete item properties."));

        verify(itemPropertiesService, never()).deleteItemProperties(itemId);
    }
}
