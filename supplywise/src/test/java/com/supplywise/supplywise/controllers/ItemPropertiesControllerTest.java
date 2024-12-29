package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.services.ItemPropertiesService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.config.JwtAuthenticationFilter;
import com.supplywise.supplywise.config.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemPropertiesController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
class ItemPropertiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemPropertiesService itemPropertiesService;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private ItemPropertiesController itemPropertiesController;

    private ObjectMapper objectMapper;

    private String managerUser;
    private String disassociatedUser;
    private ItemProperties itemProperties;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        itemId = UUID.randomUUID();
        itemProperties = new ItemProperties();
        itemProperties.setId(itemId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testCreateItemProperties_AuthorizedUser() throws Exception {
    
        // Mock the item properties creation
        when(itemPropertiesService.createItemProperties(any(ItemProperties.class))).thenReturn(itemProperties);
    
        // Make the request with the mock JWT token
        mockMvc.perform(post("/api/item-properties/create")
                .header("Authorization", "Bearer " + managerUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemProperties)))
                .andExpect(status().isCreated()) // Expect Created (201) status
                .andExpect(jsonPath("$.id").value(itemId.toString())); // Check if the response contains the item ID
    
        // Verify the item properties service was called
        verify(itemPropertiesService, times(1)).createItemProperties(any(ItemProperties.class));
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void testCreateItemProperties_DisassociatedUser() throws Exception {
    
        // Make the request with the mock JWT token
        mockMvc.perform(post("/api/item-properties/create")
                .header("Authorization", "Bearer " + disassociatedUser) // Pass the mock JWT token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemProperties)))
                .andExpect(status().isForbidden()); // Expect Forbidden (403) status
    
        // Verify that the item properties service was never called
        verify(itemPropertiesService, never()).createItemProperties(any(ItemProperties.class));
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetAllItemProperties() throws Exception {
        when(itemPropertiesService.getAllItemProperties()).thenReturn(List.of(itemProperties));

        mockMvc.perform(get("/api/item-properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId.toString()));

        verify(itemPropertiesService, times(1)).getAllItemProperties();
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetItemPropertiesById_AuthorizedUser() throws Exception {
    
        // Mock the service to return item properties by ID
        when(itemPropertiesService.getItemPropertiesById(itemId)).thenReturn(itemProperties);
    
        // Make the request with the mock JWT token
        mockMvc.perform(get("/api/item-properties/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser)) // Pass the mock JWT token
                .andExpect(status().isOk()) // Expect OK (200) status
                .andExpect(jsonPath("$.id").value(itemId.toString())); // Check that the ID in the response matches the requested ID
    
        // Verify that the item properties service was called once with the correct ID
        verify(itemPropertiesService, times(1)).getItemPropertiesById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void testGetItemPropertiesById_DisassociatedUser() throws Exception {
    
        // Make the request with the mock JWT token for the disassociated user
        mockMvc.perform(get("/api/item-properties/{id}", itemId)
                .header("Authorization", "Bearer " + disassociatedUser)) // Pass the mock JWT token
                .andExpect(status().isForbidden()); // Expect Forbidden (403) status
    
        // Verify that the item properties service was not called
        verify(itemPropertiesService, never()).getItemPropertiesById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetItemPropertiesById_NotFound() throws Exception {
    
        when(itemPropertiesService.getItemPropertiesById(itemId)).thenReturn(null); // Simulate item not found
    
        mockMvc.perform(get("/api/item-properties/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser)) // Pass the mock JWT token
                .andExpect(status().isNotFound()) // Expect Not Found (404) status
                .andExpect(content().string("Item properties ID does not exist.")); // Check error message
    
        // Verify that the item properties service was called once
        verify(itemPropertiesService, times(1)).getItemPropertiesById(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testDeleteItemProperties_AuthorizedUser() throws Exception {
        
        mockMvc.perform(delete("/api/item-properties/{id}", itemId)
                .header("Authorization", "Bearer " + managerUser)) // Pass the mock JWT token
                .andExpect(status().isNoContent()); // Expect No Content (204) status
    
        // Verify that the item properties service was called once
        verify(itemPropertiesService, times(1)).deleteItemProperties(itemId);
    }    

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void testDeleteItemProperties_DisassociatedUser() throws Exception {
        
        mockMvc.perform(delete("/api/item-properties/{id}", itemId)
                .header("Authorization", "Bearer " + disassociatedUser)) // Pass the mock JWT token
                .andExpect(status().isForbidden()); // Expect Forbidden (403) status
    
        // Verify that the item properties service was not called
        verify(itemPropertiesService, never()).deleteItemProperties(itemId);
    }    
}
