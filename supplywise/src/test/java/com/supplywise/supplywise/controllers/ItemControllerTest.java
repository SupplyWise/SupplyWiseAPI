package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.ItemService;
import com.supplywise.supplywise.services.AuthHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private ItemController itemController;

    private User authorizedUser;
    private User disassociatedUser;
    private Item item;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();

        itemId = UUID.randomUUID();
        item = new Item();
        item.setId(itemId);
        item.setName("Test Item");

        authorizedUser = new User();
        authorizedUser.setRole(Role.MANAGER);

        disassociatedUser = new User();
        disassociatedUser.setRole(Role.DISASSOCIATED);
    }

    @Test
    void testCreateItem_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemService.createItem(item)).thenReturn(item);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Item")));

        verify(itemService).createItem(item);
    }

    @Test
    void testCreateItem_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to create items."));

        verify(itemService, never()).createItem(item);
    }

    @Test
    void testGetAllItems() throws Exception {
        List<Item> items = Collections.singletonList(item);
        when(itemService.getAllItems()).thenReturn(items);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));

        verify(itemService).getAllItems();
    }

    @Test
    void testGetItemById_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemService.getItemById(itemId)).thenReturn(item);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Item")));

        verify(itemService).getItemById(itemId);
    }

    @Test
    void testGetItemById_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to fetch items."));

        verify(itemService, never()).getItemById(itemId);
    }

    @Test
    void testGetItemById_NotFound() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
        when(itemService.getItemById(itemId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item ID does not exist."));

        verify(itemService).getItemById(itemId);
    }

    @Test
    void testDeleteItem_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/item/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(itemId);
    }

    @Test
    void testDeleteItem_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/item/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to delete items."));

        verify(itemService, never()).deleteItem(itemId);
    }
}
