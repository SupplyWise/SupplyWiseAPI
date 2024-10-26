package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API for managing users")
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found", 
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        logger.info("Attempting to get user by an email");
        if (!userService.isEmailValid(email)) {
            logger.error("Invalid email");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isPresent()) {
            logger.info("User found");
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        }
        logger.error("User not found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update user by email", description = "Update an existing user by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    @PutMapping("email/{email}")
    public ResponseEntity<User> updateUser(@PathVariable String email, @RequestBody User updatedUser) {
        logger.info("Attempting to update user by email");

        // Check if the email is valid
        if (!userService.isEmailValid(email)) {
            logger.error("Invalid email");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Obtain the existing user
        Optional<User> existingUserOptional = userService.getUserByEmail(email);
        if (!existingUserOptional.isPresent()) {
            logger.error("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if the updated user is valid
        if (!userService.isUserValid(updatedUser)) {
            logger.error("Invalid updatedUser");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Update the user
        userService.updateUser(existingUserOptional.get().getId(), updatedUser);
        logger.info("User updated");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Delete user by email", description = "Delete an existing user by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username")
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
        logger.info("Attempting to delete user by email");
        if (!userService.isEmailValid(email)) {
            logger.error("Invalid email");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!userService.userExistsByEmail(email)) {
            logger.error("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteByEmail(email);
        logger.info("User deleted");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    // TODO review these as they are unnecessary for now (also, the last 2 should be admin only)
    @Operation(summary = "Get users by restaurant ID", description = "Retrieve users associated with a specific restaurant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid restaurant ID"),
        @ApiResponse(responseCode = "204", description = "No users found")
    })
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<User>> getUsersByRestaurantId(@PathVariable UUID restaurantId) {
        logger.info("Attempting to get users by restaurant id");
        // Check if the restaurant id is valid
        if (restaurantId == null) {
            logger.error("Invalid restaurant id");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<User> users = userService.getUsersByRestaurantId(restaurantId);
        if (users.isEmpty()) {
            logger.error("No users found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.info("Users found");
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get users by role", description = "Retrieve users by their role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role"),
        @ApiResponse(responseCode = "204", description = "No users found")
    })
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        logger.info("Attempting to get users by role");
        if (role == null) {
            logger.error("Invalid role");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<User> users = userService.getUsersByRole(role);
        if (users.isEmpty()) {
            logger.error("No users found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.info("Users found");
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No users found")
    })
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}