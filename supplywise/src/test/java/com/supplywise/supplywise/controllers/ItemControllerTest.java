package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.ItemService;
import com.supplywise.supplywise.services.AuthHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private ItemController itemController;

    private ObjectMapper objectMapper;

    private User managerUser;
    private User disassociatedUser;
    private Item item;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
        objectMapper = new ObjectMapper();

        itemId = UUID.randomUUID();
        item = new Item();
        item.setId(itemId);
        item.setName("Test Item");

        managerUser = new User();
        managerUser.setRole(Role.MANAGER);

        disassociatedUser = new User();
        disassociatedUser.setRole(Role.DISASSOCIATED);
    }

    @Test
    void createItem_whenUserIsManager_shouldReturnCreated() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.createItem(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/item/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService, times(1)).createItem(any(Item.class));
    }

    @Test
    void createItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(post("/api/item/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to create items."));

        verify(itemService, never()).createItem(any(Item.class));
    }

    @Test
    void getAllItems_shouldReturnListOfItems() throws Exception {
        List<Item> items = Collections.singletonList(item);
        when(itemService.getAllItems()).thenReturn(items);

        mockMvc.perform(get("/api/item")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name").value("Test Item"));

        verify(itemService, times(1)).getAllItems();
    }

    @Test
    void getItemById_whenUserIsManager_shouldReturnItem() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.getItemById(itemId)).thenReturn(item);

        mockMvc.perform(get("/api/item/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService, times(1)).getItemById(itemId);
    }

    @Test
    void getItemById_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(get("/api/item/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to fetch items."));

        verify(itemService, never()).getItemById(itemId);
    }

    @Test
    void getItemById_whenItemNotFound_shouldReturnNotFound() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.getItemById(itemId)).thenReturn(null);

        mockMvc.perform(get("/api/item/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item ID does not exist."));

        verify(itemService, times(1)).getItemById(itemId);
    }

    @Test
    void deleteItem_whenUserIsManager_shouldReturnNoContent() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);

        mockMvc.perform(delete("/api/item/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).deleteItem(itemId);
    }

    @Test
    void deleteItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(delete("/api/item/{id}", itemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to delete items."));

        verify(itemService, never()).deleteItem(itemId);
    }
}
