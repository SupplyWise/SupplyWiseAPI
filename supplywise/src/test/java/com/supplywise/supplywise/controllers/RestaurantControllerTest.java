package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.services.RestaurantService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private CompanyService companyService;

    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private RestaurantController restaurantController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(restaurantController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateRestaurant_Success() throws Exception {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        Restaurant restaurant = new Restaurant();
        restaurant.setCompany(company);
        restaurant.setName("Test Restaurant");

        when(companyService.companyExists(companyId)).thenReturn(true);
        when(restaurantService.saveRestaurant(any(Restaurant.class))).thenReturn(restaurant);

        mockMvc.perform(post("/api/restaurants/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
        
        verify(restaurantService, times(1)).saveRestaurant(any(Restaurant.class));
    }


    @Test
    void testCreateRestaurant_InvalidCompany() throws Exception {
        Restaurant restaurant = new Restaurant();
        restaurant.setCompany(new Company()); // Setting an invalid company without an ID

        mockMvc.perform(post("/api/restaurants/")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(restaurant)))
                .andExpect(status().isBadRequest());
        
        verify(restaurantService, never()).saveRestaurant(any(Restaurant.class));
    }

    @Test
    void testGetRestaurantById_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");

        when(restaurantService.getRestaurantById(any(UUID.class))).thenReturn(Optional.of(restaurant));

        mockMvc.perform(get("/api/restaurants/" + restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
        
        verify(restaurantService, times(1)).getRestaurantById(any(UUID.class));
    }

    @Test
    void testGetRestaurantById_NotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.getRestaurantById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/restaurants/" + restaurantId))
                .andExpect(status().isNotFound());
        
        verify(restaurantService, times(1)).getRestaurantById(any(UUID.class));
    }

    @Test
    void testUpdateRestaurant_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");

        String newName = "Updated Restaurant Name";

        when(restaurantService.updateRestaurantName(any(UUID.class), anyString())).thenReturn(Optional.of(restaurant));

        mockMvc.perform(put("/api/restaurants/" + restaurantId)
                        .contentType("text/plain")
                        .content(newName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
        
        verify(restaurantService, times(1)).updateRestaurantName(any(UUID.class), anyString());
    }

    @Test
    void testUpdateRestaurant_NotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        String newName = "Updated Restaurant Name";

        when(restaurantService.updateRestaurantName(any(UUID.class), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/restaurants/" + restaurantId)
                        .contentType("text/plain")
                        .content(newName))
                .andExpect(status().isNotFound());
        
        verify(restaurantService, times(1)).updateRestaurantName(any(UUID.class), anyString());
    }

    @Test
    void testDeleteRestaurantById_Success() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.restaurantExistsById(any(UUID.class))).thenReturn(true);

        mockMvc.perform(delete("/api/restaurants/" + restaurantId))
                .andExpect(status().isOk());
        
        verify(restaurantService, times(1)).deleteRestaurantById(any(UUID.class));
    }

    @Test
    void testDeleteRestaurantById_NotFound() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.restaurantExistsById(any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete("/api/restaurants/" + restaurantId))
                .andExpect(status().isNotFound());
        
        verify(restaurantService, times(1)).restaurantExistsById(any(UUID.class));
    }

    @Test
    void testGetRestaurantsByCompanyId_Success() throws Exception {
        UUID companyId = UUID.randomUUID();
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Restaurant 1");
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Restaurant 2");
        restaurants.add(restaurant1);
        restaurants.add(restaurant2);

        when(restaurantService.getRestaurantsByCompanyId(any(UUID.class))).thenReturn(restaurants);

        mockMvc.perform(get("/api/restaurants/company/" + companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Restaurant 1"))
                .andExpect(jsonPath("$[1].name").value("Restaurant 2"));
        
        verify(restaurantService, times(1)).getRestaurantsByCompanyId(any(UUID.class));
    }

    @Test
    void testGetRestaurantsByCompanyId_NoContent() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(restaurantService.getRestaurantsByCompanyId(any(UUID.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/restaurants/company/" + companyId))
                .andExpect(status().isNoContent());
        
        verify(restaurantService, times(1)).getRestaurantsByCompanyId(any(UUID.class));
    }

    @Test
    void testGetRestaurants_Success() throws Exception {
        UUID companyId = UUID.randomUUID();
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Restaurant 1");
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Restaurant 2");
        restaurants.add(restaurant1);
        restaurants.add(restaurant2);

        when(restaurantService.getRestaurantsByCompanyId(any(UUID.class))).thenReturn(restaurants);
        when(authHandler.getAuthenticatedCompanyId()).thenReturn(companyId.toString());

        mockMvc.perform(get("/api/restaurants/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Restaurant 1"))
                .andExpect(jsonPath("$[1].name").value("Restaurant 2"));
        
        verify(restaurantService, times(1)).getRestaurantsByCompanyId(any(UUID.class));
    }

    @Test
    void testGetRestaurants_NoContent() throws Exception {
        when(restaurantService.getRestaurantsByCompanyId(any(UUID.class))).thenReturn(new ArrayList<>());
        when(authHandler.getAuthenticatedCompanyId()).thenReturn(UUID.randomUUID().toString());

        mockMvc.perform(get("/api/restaurants/company"))
                .andExpect(status().isNoContent());
        
        verify(restaurantService, times(1)).getRestaurantsByCompanyId(any(UUID.class));
    }
}
