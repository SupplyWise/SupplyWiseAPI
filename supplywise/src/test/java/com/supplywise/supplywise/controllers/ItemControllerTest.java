package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.services.ItemService;
import com.supplywise.supplywise.services.AuthHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.config.JwtAuthenticationFilter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private ItemController itemController;

    private ObjectMapper objectMapper;

    private String managerUser;
    private String disassociatedUser;
    private Item item;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        itemId = UUID.randomUUID();
        item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void createItem_whenUserIsManager_shouldReturnCreated() throws Exception {
    
        // Mock the item creation service
        when(itemService.createItem(any(Item.class))).thenReturn(item);
    
        mockMvc.perform(post("/api/item/create")
                .header("Authorization", "Bearer " + managerUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Item"));
    
        // Verify that the item creation service was called
        verify(itemService, times(1)).createItem(any(Item.class));
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void createItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
    
        // Make the request with the mock JWT token
        mockMvc.perform(post("/api/item/create")
                .header("Authorization", "Bearer " + disassociatedUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isForbidden());
    
        // Verify that the item creation service was not called
        verify(itemService, never()).createItem(any(Item.class));
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
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
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void getItemById_whenUserIsManager_shouldReturnItem() throws Exception {
    
        // Mock the item retrieval service
        when(itemService.getItemById(itemId)).thenReturn(item);
    
        // Make the request with the mock JWT token
        mockMvc.perform(get("/api/item/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));
    
        // Verify the item service was called once
        verify(itemService, times(1)).getItemById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void getItemById_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
    
        // Make the request with the mock JWT token
        mockMvc.perform(get("/api/item/{id}", itemId)
                .header("Authorization", "Bearer " + disassociatedUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    
        // Verify the item service was never called
        verify(itemService, never()).getItemById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void getItemById_whenItemNotFound_shouldReturnNotFound() throws Exception {
    
        // Make the request with the mock JWT token
        when(itemService.getItemById(itemId)).thenReturn(null);
    
        mockMvc.perform(get("/api/item/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item ID does not exist."));
    
        // Verify the item service was called exactly once
        verify(itemService, times(1)).getItemById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void deleteItem_whenUserIsManager_shouldReturnNoContent() throws Exception {
    
        // Make the request with the mock JWT token
        mockMvc.perform(delete("/api/item/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    
        // Verify the item service was called exactly once to delete the item
        verify(itemService, times(1)).deleteItem(itemId);
    }    
    
    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void deleteItem_whenUserIsDisassociated_shouldReturnForbidden() throws Exception {
    
        // Make the request with the mock JWT token
        mockMvc.perform(delete("/api/item/{id}", itemId)
                .header("Authorization", "Bearer " + disassociatedUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Expect Forbidden status
    
        // Verify the item service was never called since the user is disassociated
        verify(itemService, never()).deleteItem(itemId);
    }    
}
