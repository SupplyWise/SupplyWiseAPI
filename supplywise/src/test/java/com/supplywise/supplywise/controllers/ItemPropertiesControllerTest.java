package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.ItemPropertiesService;
import com.supplywise.supplywise.services.AuthHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemPropertiesControllerTest {

        private MockMvc mockMvc;

        @Mock
        private ItemPropertiesService itemPropertiesService;

        @Mock
        private AuthHandler authHandler;

        @InjectMocks
        private ItemPropertiesController itemPropertiesController;

        private ObjectMapper objectMapper;

        private User authorizedUser;
        private User managerMasterUser;
        private User franchiseOwnerUser;
        private User disassociatedUser;
        private ItemProperties itemProperties;
        private UUID itemId;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                mockMvc = MockMvcBuilders.standaloneSetup(itemPropertiesController).setValidator(null).build();

                objectMapper = new ObjectMapper();

                itemId = UUID.randomUUID();
                itemProperties = new ItemProperties();
                itemProperties.setId(itemId);

                authorizedUser = new User();
                authorizedUser.setRole(Role.MANAGER);

                managerMasterUser = new User();
                managerMasterUser.setRole(Role.MANAGER_MASTER);

                franchiseOwnerUser = new User();
                franchiseOwnerUser.setRole(Role.FRANCHISE_OWNER);

                disassociatedUser = new User();
                disassociatedUser.setRole(Role.DISASSOCIATED);
        }

        @Test
        void testCreateItemProperties_AuthorizedUser() throws Exception {
                when(authHandler.getAuthenticatedUser()).thenReturn(authorizedUser);
                when(itemPropertiesService.createItemProperties(any(ItemProperties.class))).thenReturn(itemProperties);

                mockMvc.perform(post("/api/item-properties/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemProperties)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(itemId.toString()));

                verify(itemPropertiesService, times(1)).createItemProperties(any(ItemProperties.class));
        }

        @Test
        void testCreateItemProperties_DisassociatedUser() throws Exception {
                when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

                mockMvc.perform(post("/api/item-properties/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemProperties)))
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
                                .andExpect(content().string("Item properties ID does not exist."));

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

        @Test
        void testUpdateMinimumStockQuantity_InvalidQuantity() throws Exception {
        // Simulate an invalid quantity (negative number)
        when(authHandler.getAuthenticatedUser()).thenReturn(managerMasterUser);

        mockMvc.perform(put("/api/item-properties/{id}/minimum-stock", itemId)
                        .param("minimumStock", "-5")) // Simulating invalid input
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Minimum stock must be non-negative"));

        // Ensure the service method is not called with invalid quantity
        verify(itemPropertiesService, never()).updateMinimumStockQuantity(eq(itemId), eq(-5));
        }


        @Test
        void testUpdateMinimumStockQuantity_AuthorizedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(managerMasterUser);
        when(itemPropertiesService.updateMinimumStockQuantity(eq(itemId), eq(10)))
                .thenReturn(itemProperties);

        mockMvc.perform(put("/api/item-properties/{id}/minimum-stock", itemId)
                        .param("minimumStock", "10")) // Simulating valid input
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()));

        verify(itemPropertiesService, times(1)).updateMinimumStockQuantity(eq(itemId), eq(10));
        }

        @Test
        void testUpdateMinimumStockQuantity_DisassociatedUser() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(disassociatedUser);

        mockMvc.perform(put("/api/item-properties/{id}/minimum-stock", itemId)
                        .param("minimumStock", "10"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only MANAGER_MASTER or FRANCHISE_OWNER users can update minimum stock quantity."));

        verify(itemPropertiesService, never()).updateMinimumStockQuantity(any(UUID.class), anyInt());
        }

        @Test
        void testUpdateMinimumStockQuantity_ItemNotFound() throws Exception {
        when(authHandler.getAuthenticatedUser()).thenReturn(franchiseOwnerUser);
        when(itemPropertiesService.updateMinimumStockQuantity(eq(itemId), eq(10))).thenReturn(null);

        mockMvc.perform(put("/api/item-properties/{id}/minimum-stock", itemId)
                        .param("minimumStock", "10"))
                .andExpect(status().isNotFound());

        verify(itemPropertiesService, times(1)).updateMinimumStockQuantity(eq(itemId), eq(10));
        }

}
