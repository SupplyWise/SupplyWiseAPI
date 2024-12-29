package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supplywise.supplywise.DAO.AddItemToInventoryRequest;
import com.supplywise.supplywise.DAO.CreateInventoryRequest;
import com.supplywise.supplywise.model.Inventory;
import com.supplywise.supplywise.model.Item;
import com.supplywise.supplywise.model.ItemProperties;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.InventoryService;
import com.supplywise.supplywise.services.ItemPropertiesService;
import com.supplywise.supplywise.services.ItemService;
import com.supplywise.supplywise.services.RestaurantService;
import com.supplywise.supplywise.config.JwtAuthenticationFilter;
import com.supplywise.supplywise.config.SecurityConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(InventoryController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemPropertiesService itemPropertiesService;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private InventoryController inventoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Helper method to create a sample CreateInventoryRequest
    private CreateInventoryRequest createInventoryRequest(UUID restaurantId) {
        return CreateInventoryRequest.builder()
                .restaurantId(restaurantId)
                .emissionDate(LocalDateTime.now())
                .expectedClosingDate(LocalDateTime.now().plusDays(14))
                .build();
    }

    // Helper method to create a sample inventory
    private Inventory createInventory(UUID restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setRestaurant(restaurant);
        inventory.setEmissionDate(LocalDateTime.now());
        inventory.setClosingDate(LocalDateTime.now().plusDays(7));
        inventory.setExpectedClosingDate(LocalDateTime.now().plusDays(14));
        inventory.setReport("Test report");
        return inventory;
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testCreateInventory_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        CreateInventoryRequest request = createInventoryRequest(restaurantId);
        Inventory inventory = createInventory(restaurantId);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(inventoryService.saveInventory(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/api/inventories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.report").value("Test report"));

        verify(inventoryService, times(1)).saveInventory(any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testCreateInventory_InvalidRestaurant() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        CreateInventoryRequest request = createInventoryRequest(restaurantId);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/inventories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetInventoryById_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setReport("Test report");

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Test report"));

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetInventoriesByRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        List<Inventory> inventories = List.of(createInventory(restaurantId), createInventory(restaurantId));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].report").value("Test report"))
                .andExpect(jsonPath("$[1].report").value("Test report"));

        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetInventoriesByRestaurant_RestaurantNotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetInventoriesByRestaurant_NoContent() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(any(Restaurant.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetItemsByInventoryId_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        
        Item item1 = new Item();
        item1.setId(UUID.randomUUID());
        item1.setName("Test Item 1");
        
        Item item2 = new Item();
        item2.setId(UUID.randomUUID());
        item2.setName("Test Item 2");
        
        ItemProperties itemProperties1 = new ItemProperties(item1, LocalDate.now().plusMonths(6), 10);
        ItemProperties itemProperties2 = new ItemProperties(item2, LocalDate.now().plusMonths(6), 20);
        
        inventory.addItemProperties(itemProperties1);
        inventory.addItemProperties(itemProperties2);

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/items")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetItemsByInventoryId_NoItems() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/items")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetItemsByInventoryId_InventoryNotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/items")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testDeleteInventoryById_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(new Inventory()));

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).deleteInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testDeleteInventoryById_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).deleteInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testUpdateInventory_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = createInventory(restaurantId);
        inventory.setId(inventoryId);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryService.updateInventory(eq(inventoryId), any(Inventory.class))).thenReturn(Optional.of(inventory));

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Test report"));

        verify(inventoryService, times(1)).updateInventory(eq(inventoryId), any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testUpdateInventory_InvalidRestaurant() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = createInventory(restaurantId);
        inventory.setId(inventoryId);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(false);

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).updateInventory(any(), any());
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testUpdateInventory_NullRestaurant() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setId(inventoryId);
        inventory.setRestaurant(null);

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).updateInventory(any(), any());
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testUpdateInventory_UpdateFailed() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = createInventory(restaurantId);
        inventory.setId(inventoryId);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryService.updateInventory(eq(inventoryId), any(Inventory.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isInternalServerError());

        verify(inventoryService, times(1)).updateInventory(eq(inventoryId), any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testAddItemToInventory_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(inventoryId);

        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Test Item");

        AddItemToInventoryRequest itemRequest = new AddItemToInventoryRequest();
        itemRequest.setBarCode(123456);
        itemRequest.setQuantity(10);
        itemRequest.setExpirationDate(LocalDate.now().plusMonths(6));

        when(itemService.findItemByBarcode(itemRequest.getBarCode())).thenReturn(item);
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryService.saveInventory(any(Inventory.class))).thenReturn(inventory);

        ItemProperties itemProperties = new ItemProperties(item, itemRequest.getExpirationDate(), itemRequest.getQuantity());
        when(itemPropertiesService.createItemProperties(any(ItemProperties.class))).thenReturn(itemProperties);

        mockMvc.perform(post("/api/inventories/" + inventoryId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findItemByBarcode(itemRequest.getBarCode());
        verify(itemPropertiesService, times(1)).createItemProperties(any(ItemProperties.class));
        verify(inventoryService, times(1)).saveInventory(any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testUpdateInventory_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setRestaurant(restaurant);

        when(restaurantService.restaurantExistsById(restaurantId)).thenReturn(true);
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/inventories/" + inventoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).updateInventory(eq(inventoryId), any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testAddItemToInventory_InventoryNotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        AddItemToInventoryRequest itemRequest = new AddItemToInventoryRequest();
        itemRequest.setBarCode(123456); // Insira um código de barras fictício
        itemRequest.setQuantity(10); // Defina a quantidade

        Item item = new Item();
        when(itemService.findItemByBarcode(itemRequest.getBarCode())).thenReturn(item);
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/inventories/" + inventoryId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isNotFound());

        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryClosed_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setClosingDate(LocalDateTime.now().minusDays(1)); // Set a past closing date to simulate closure

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/is-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true)); // Adjusted to expect a direct Boolean value

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryClosed_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/is-closed"))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryClosed_NoClosingDate() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setClosingDate(null);

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/is-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryExpectedToClose_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setExpectedClosingDate(LocalDateTime.now().minusDays(1)); // Set a past expected closing date

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/should-be-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true)); // Adjusted to expect a direct Boolean value

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryExpectedToClose_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/should-be-closed"))
                .andExpect(status().isNotFound());

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testIsInventoryExpectedToClose_NoExpectedClosingDate() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        inventory.setExpectedClosingDate(null);

        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventories/" + inventoryId + "/should-be-closed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(inventoryService, times(1)).getInventoryById(inventoryId);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetOpenInventoriesByRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        
        // Create inventories (one open, one closed)
        Inventory openInventory = createInventory(restaurantId);
        openInventory.setClosingDate(null);  // Set to future date
        
        Inventory closedInventory = createInventory(restaurantId);
        closedInventory.setClosingDate(LocalDateTime.now().minusDays(1));  // Set to past date
        
        List<Inventory> inventories = List.of(openInventory, closedInventory);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(restaurant)).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].report").value("Test report"));

        verify(inventoryService, times(1)).getInventoriesByRestaurant(restaurant);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetOpenInventoriesByRestaurant_NoOpenInventories() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        
        // Create closed inventories (all have past closing dates)
        Inventory closedInventory1 = createInventory(restaurantId);
        closedInventory1.setClosingDate(LocalDateTime.now().minusDays(1));
        
        Inventory closedInventory2 = createInventory(restaurantId);
        closedInventory2.setClosingDate(LocalDateTime.now().minusDays(2));
        
        List<Inventory> inventories = List.of(closedInventory1, closedInventory2);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(inventoryService.getInventoriesByRestaurant(restaurant)).thenReturn(inventories);

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isNoContent());  // No open inventories found

        verify(inventoryService, times(1)).getInventoriesByRestaurant(restaurant);
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testGetOpenInventoriesByRestaurant_RestaurantNotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        
        // Mock that the restaurant does not exist
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventories/restaurant/" + restaurantId + "/open"))
                .andExpect(status().isNotFound());  // Restaurant not found

        verify(inventoryService, never()).getInventoriesByRestaurant(any(Restaurant.class));
    }

    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testCloseInventory_Success() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        Inventory inventory = createInventory(UUID.randomUUID());
        LocalDateTime closingDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Mock service methods
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryService.saveInventory(any(Inventory.class))).thenReturn(inventory);
        when(authHandler.getAuthenticatedCognitoSub()).thenReturn("cognito-sub-example");

        mockMvc.perform(put("/api/inventories/" + inventoryId + "/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closingDate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closingDate").value(closingDate.toString())) // Truncate nanoseconds
                .andExpect(jsonPath("$.closedByUser").value("cognito-sub-example"));

        // Verify service calls
        verify(inventoryService, times(1)).getInventoryById(inventoryId);
        verify(inventoryService, times(1)).saveInventory(any(Inventory.class));
    }


    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"MANAGER"})
    void testCloseInventory_NotFound() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        LocalDateTime closingDate = LocalDateTime.now();

        // Mock the repository to simulate inventory not found
        when(inventoryService.getInventoryById(inventoryId)).thenReturn(Optional.empty());

        // Perform the request with the mock token
        mockMvc.perform(put("/api/inventories/" + inventoryId + "/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closingDate)))
                .andExpect(status().isNotFound());

        // Verify service calls
        verify(inventoryService, times(1)).getInventoryById(inventoryId);
        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }


    @Test
    @WithMockUser(username = "cognito-sub-example", roles = {"DISASSOCIATED"})
    void testCloseInventory_Unauthorized() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        LocalDateTime closingDate = LocalDateTime.now();
    
        mockMvc.perform(put("/api/inventories/" + inventoryId + "/close")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(closingDate)))
            .andExpect(status().isForbidden());
    
        // Verify that no inventory service method was called
        verify(inventoryService, never()).getInventoryById(any());
        verify(inventoryService, never()).saveInventory(any(Inventory.class));
    }    

}