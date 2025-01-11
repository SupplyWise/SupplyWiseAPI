package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.model.InventoryPeriodicity;
import com.supplywise.supplywise.repositories.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveRestaurant_ShouldSaveRestaurant() {
        // Given
        Company company = new Company();
        company.setId(UUID.randomUUID());
        
        Restaurant restaurant = new Restaurant("Test Restaurant", company);

        // Mock the repository
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(savedRestaurant);
        assertEquals("Test Restaurant", savedRestaurant.getName());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testGetRestaurantById_Exists_ShouldReturnRestaurant() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        Company company = new Company();
        company.setId(UUID.randomUUID());
        Restaurant restaurant = new Restaurant("Test Restaurant", company);
        restaurant.setId(restaurantId);

        // Mock the repository
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When
        Optional<Restaurant> result = restaurantService.getRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(restaurantId, result.get().getId());
        assertEquals("Test Restaurant", result.get().getName());
    }

    @Test
    void testGetRestaurantById_NotExists_ShouldReturnEmpty() {
        // Given
        UUID restaurantId = UUID.randomUUID();

        // Mock the repository
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When
        Optional<Restaurant> result = restaurantService.getRestaurantById(restaurantId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateRestaurantName_Exists_ShouldUpdateName() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        Company company = new Company();
        company.setId(UUID.randomUUID());
        Restaurant restaurant = new Restaurant("Old Restaurant Name", company);
        restaurant.setId(restaurantId);

        // Mock the repository
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        Optional<Restaurant> result = restaurantService.updateRestaurantName(restaurantId, "New Restaurant Name");

        // Then
        assertTrue(result.isPresent());
        assertEquals("New Restaurant Name", result.get().getName());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testUpdateRestaurantName_NotExists_ShouldReturnEmpty() {
        // Given
        UUID restaurantId = UUID.randomUUID();

        // Mock the repository
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When
        Optional<Restaurant> result = restaurantService.updateRestaurantName(restaurantId, "New Restaurant Name");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteRestaurantById_Exists_ShouldDelete() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        // When
        restaurantService.deleteRestaurantById(restaurantId);

        // Then
        verify(restaurantRepository, times(1)).deleteById(restaurantId);
    }

    @Test
    void testDeleteRestaurantById_NotExists_ShouldNotDelete() {
        // Given
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        // When
        restaurantService.deleteRestaurantById(restaurantId);

        // Then
        verify(restaurantRepository, never()).deleteById(any());
    }

    @Test
    void testGetRestaurantsByCompanyId_Exists_ShouldReturnList() {
        // Given
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        Restaurant restaurant1 = new Restaurant("Restaurant 1", company);
        Restaurant restaurant2 = new Restaurant("Restaurant 2", company);
        when(restaurantRepository.findByCompanyId(companyId)).thenReturn(List.of(restaurant1, restaurant2));

        // When
        List<Restaurant> result = restaurantService.getRestaurantsByCompanyId(companyId);

        // Then
        assertEquals(2, result.size());
        assertEquals("Restaurant 1", result.get(0).getName());
        assertEquals("Restaurant 2", result.get(1).getName());
    }

    @Test
    void testGetRestaurantsByCompanyId_NotExists_ShouldReturnEmptyList() {
        // Given
        UUID companyId = UUID.randomUUID();
        when(restaurantRepository.findByCompanyId(companyId)).thenReturn(List.of());

        // When
        List<Restaurant> result = restaurantService.getRestaurantsByCompanyId(companyId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveRestaurant_WithPeriodicity_ShouldSaveRestaurant() {
        // Given
        Company company = new Company();
        company.setId(UUID.randomUUID());
        
        Restaurant restaurant = new Restaurant("Test Restaurant", company);
        restaurant.setPeriodicity(InventoryPeriodicity.WEEKLY);

        // Mock the repository
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(savedRestaurant);
        assertEquals("Test Restaurant", savedRestaurant.getName());
        assertEquals(InventoryPeriodicity.WEEKLY, savedRestaurant.getPeriodicity());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testSaveRestaurant_WithCustomPeriodicity_ShouldSaveRestaurant() {
        // Given
        Company company = new Company();
        company.setId(UUID.randomUUID());
        
        Restaurant restaurant = new Restaurant("Test Restaurant", company);
        restaurant.setPeriodicity(InventoryPeriodicity.CUSTOM);
        restaurant.setCustomInventoryPeriodicity(14);

        // Mock the repository
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(savedRestaurant);
        assertEquals("Test Restaurant", savedRestaurant.getName());
        assertEquals(InventoryPeriodicity.CUSTOM, savedRestaurant.getPeriodicity());
        assertEquals(14, savedRestaurant.getCustomInventoryPeriodicity());
        verify(restaurantRepository, times(1)).save(restaurant);
    }

    @Test
    void testSaveRestaurant_WithNoPeriodicity_ShouldSetDefaultNull() {
        // Given
        Company company = new Company();
        company.setId(UUID.randomUUID());
        
        Restaurant restaurant = new Restaurant("Test Restaurant", company);

        // Mock the repository
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // When
        Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);

        // Then
        assertNotNull(savedRestaurant);
        assertEquals(InventoryPeriodicity.NULL, savedRestaurant.getPeriodicity());
        verify(restaurantRepository, times(1)).save(restaurant);
    }
}
