package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private ItemController itemController;

    private User managerUser;
    private User disassociatedUser;
    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        managerUser = new User();
        managerUser.setRole(Role.MANAGER);

        disassociatedUser = new User();
        disassociatedUser.setRole(Role.DISASSOCIATED);

        item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Test Item");
    }

    @Test
    void createItem_whenUserIsManager_shouldReturnCreated() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.createItem(item)).thenReturn(item);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Item")));
    }

    @Test
    void createItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to create items."));
    }

    @Test
    void getAllItems_shouldReturnListOfItems() throws Exception {
        List<Item> items = Collections.singletonList(item);
        when(itemService.getAllItems()).thenReturn(items);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));
    }

    @Test
    void getItemById_whenUserIsManager_shouldReturnItem() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.getItemById(item.getId())).thenReturn(item);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Item")));
    }

    @Test
    void getItemById_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to fetch items."));
    }

    @Test
    void getItemById_whenItemNotFound_shouldReturnNotFound() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);
        when(itemService.getItemById(item.getId())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/item/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item ID does not exist."));
    }

    @Test
    void deleteItem_whenUserIsManager_shouldReturnNoContent() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/item/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/item/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is not authorized to delete items."));
    }
}
