package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.model.Company;
import com.supplywise.supplywise.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_ShouldEncodePasswordAndSave() {
        // User data
        User user = new User();
        user.setFullname("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(Role.DISASSOCIATED);

        // Mock the repository
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Execute the method
        User createdUser = userService.createUser(user);

        // Check if password was encoded
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertTrue(passwordEncoder.matches("password123", createdUser.getPassword()));

        // Check if the user was saved (theoretically)
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testLoadUserByEmail_UserExists_ShouldReturnUserDetails() {
        // User data
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.DISASSOCIATED);

        // Mock the repository
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Execute the method
        var userDetails = userService.loadUserByEmail("john@example.com");

        // Check if the user details are correct
        assertEquals("john@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_DISASSOCIATED")));
    }


    @Test
    void testIsUserValid_InvalidEmail_ShouldReturnFalse() {
        // User with invalid email
        User user = new User();
        user.setFullname("John Doe");
        user.setEmail("invalid_email");
        user.setPassword("password123");

        // Executa the method and check the result
        assertFalse(userService.isUserValid(user));
    }

    @Test
    void testUpdateUser_UserExists_ShouldUpdateUser() {
        // Existing user data
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setFullname("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("password123");
        existingUser.setRole(Role.DISASSOCIATED);

        // Updated user data
        User updatedUser = new User();
        updatedUser.setFullname("John Updated");
        updatedUser.setEmail("johnupdated@example.com");
        updatedUser.setPassword("newPassword");
        updatedUser.setRole(Role.MANAGER);

        // Mock the repository
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Execute the method
        Optional<User> result = userService.updateUser(userId, updatedUser);

        // Check if the user was updated
        assertTrue(result.isPresent());
        User resultUser = result.get();
        assertEquals("John Updated", resultUser.getFullname());
        assertEquals("johnupdated@example.com", resultUser.getEmail());
        assertEquals(Role.MANAGER, resultUser.getRole());
        assertEquals(resultUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    void testUpdateUser_UserDoesNotExist_ShouldReturnEmpty() {
        // Mock the repository
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute the method and check the result
        Optional<User> result = userService.updateUser(userId, new User());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCompanyDetails_UserIsManager_ShouldReturnCompanyDetails() {
        Company company = new Company();
        company.setName("Company Name");

        // User data
        User user = new User();
        user.setRole(Role.MANAGER);
        user.setRestaurant(null);
        user.setCompany(company);

        // Mock the repository
        when(userRepository.findByCompanyId(company.getId())).thenReturn(company);

        // Execute the method
        Company companyFetched = userService.getCompanyDetails(company.getId());

        // Check the result
        assertEquals("Company Name", companyFetched.getName());
    }
}
