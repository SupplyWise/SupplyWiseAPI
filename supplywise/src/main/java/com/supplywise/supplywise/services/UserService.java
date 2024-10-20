package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private int MIN_FULLNAME_LENGTH = 5;
    private int MAX_FULLNAME_LENGTH = 255;
    private int MIN_EMAIL_LENGTH = 5;
    private int MAX_EMAIL_LENGTH = 100;
    private int MIN_PASSWORD_LENGTH = 8;
    private int MAX_PASSWORD_LENGTH = 80;
    private String EMAIL_REGEX = "[a-z][a-z0-9._+-]+@[a-z]+\\.[a-z]{2,6}";

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Create a new user
    public User createUser(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Get a user by email (which is unique)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Load user by email for JWT authentication
    public UserDetails loadUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .build();
    }

    // Check if a user exists by email
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Update an existing user
    public Optional<User> updateUser(UUID id, User updatedUser) {
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
        if (newRestaurant!= null && !newRestaurant.equals(existingRestaurant)) {
            existingUser.setRestaurant(newRestaurant);
        }

        return Optional.of(userRepository.save(existingUser));
    }

    // Delete user by their email (which is unique)
    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    // Get users by restaurant id
    public List<User> getUsersByRestaurantId(UUID restaurantId) {
        return userRepository.findByRestaurantId(restaurantId);
    }

    /* Admin methods */

    // Get all users by role
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Get all users
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /* Helper functions */

    public boolean isUserValid(User user) {
        String fullname = user.getFullname();
        String email = user.getEmail();
        String password = user.getPassword();

        if (fullname == null || fullname.length() < MIN_FULLNAME_LENGTH || fullname.length() > MAX_FULLNAME_LENGTH) {
            System.out.println("Fullname is invalid");
            return false;
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            System.out.println("Password is invalid!" + password + " " + password.length());
            return false;
        }
        return isEmailValid(email);
    }

    public boolean isEmailValid(String email) {
        if (email == null) {
            System.out.println("Email is null");
            return false;
        }
        return (email.length() > MIN_EMAIL_LENGTH && email.length() < MAX_EMAIL_LENGTH && email.matches(EMAIL_REGEX));
    }

    public boolean isPasswordCorrect(String password, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, encodedPassword);
    }
}