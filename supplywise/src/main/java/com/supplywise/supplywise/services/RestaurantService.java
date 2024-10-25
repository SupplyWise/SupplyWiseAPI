package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    // Get a restaurant by ID
    public Optional<Restaurant> getRestaurantById(UUID id) {
        return restaurantRepository.findById(id);
    }

    // Create a new restaurant
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // Check if a restaurant exists by ID
    public boolean restaurantExistsById(UUID id) {
        return restaurantRepository.existsById(id);
    }

    // Update a restaurant's name (other fields stay the same)
    public Optional<Restaurant> updateRestaurantName(UUID id, String newName) {
        Optional<Restaurant> restaurantOptional = restaurantRepository.findById(id);

        if (restaurantOptional.isPresent()) {
            Restaurant restaurant = restaurantOptional.get();
            restaurant.setName(newName);
            return Optional.of(restaurantRepository.save(restaurant));
        }

        return Optional.empty();
    }

    // Delete a restaurant by ID
    public void deleteRestaurantById(UUID id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
        }
    }

    // Get all restaurants by company ID
    public List<Restaurant> getRestaurantsByCompanyId(UUID companyId) {
        return restaurantRepository.findByCompanyId(companyId);
    }
}
