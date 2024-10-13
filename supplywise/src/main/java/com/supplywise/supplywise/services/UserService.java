package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class UserService{

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // Create a new user
    public User createUser(User user){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Get a user by email (which is unique)
    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    // Check if a user exists by email
    public boolean userExistsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    // Update an existing user
    public Optional<User> updateUser(UUID id, User updatedUser){
        Optional<User> optionalExistingUser = userRepository.findById(id);
        if (!optionalExistingUser.isPresent()) {
            return Optional.empty();
        }
        
        // Get the existing user's attributes
        User existingUser = optionalExistingUser.get();
        String existingFullname = existingUser.getFullname();
        String existingEmail = existingUser.getEmail();
        String existingPassword = existingUser.getPassword();
        Role existingRole = existingUser.getRole();
        Restaurant existingRestaurant = existingUser.getRestaurant();

        // Get the updated user's attributes
        String newFullname = updatedUser.getFullname();
        String newEmail = updatedUser.getEmail();
        String newPassword = updatedUser.getPassword();
        Role newRole = updatedUser.getRole();
        Restaurant newRestaurant = updatedUser.getRestaurant();

        // Update the user attributes that are new
        if (!newFullname.equals(existingFullname)) {
            existingUser.setFullname(newFullname);
        }
        if (!newEmail.equals(existingEmail)) {
            existingUser.setEmail(newEmail);
        }
        if (!newPassword.equals(existingPassword)) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encoded = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encoded);
        }
        if (!newRole.equals(existingRole)) {
            existingUser.setRole(newRole);
        }
        if (!newRestaurant.equals(existingRestaurant)) {
            existingUser.setRestaurant(newRestaurant);
        }

        return Optional.of(userRepository.save(existingUser));
    }

    // Delete user by their email (which is unique)
    public void deleteByEmail(String email){
        userRepository.deleteByEmail(email);
    }

    // Get users by restaurant id
    public List<User> getUsersByRestaurantId(UUID restaurantId) {
        return userRepository.findByRestaurantId(restaurantId);
    }

    /* Admin methods */

    // Get all users by role
    public List<User> getUsersByRole(Role role){
        return userRepository.findByRole(role);
    }

    // Get all users
    public Page<User> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable);
    }
}