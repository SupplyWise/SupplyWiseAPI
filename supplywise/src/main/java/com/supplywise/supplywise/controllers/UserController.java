package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private int MIN_FULLNAME_LENGTH = 5;
    private int MAX_FULLNAME_LENGTH = 255;
    private int MIN_EMAIL_LENGTH = 5;
    private int MAX_EMAIL_LENGTH = 100;
    private int MIN_PASSWORD_LENGTH = 8;
    private int MAX_PASSWORD_LENGTH = 80;
    private String EMAIL_REGEX = "[a-z][a-z0-9._+-]+@[a-z]+\\.[a-z]{2,6}";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully", 
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Attempting to create a new user");

        // Check if the user is valid
        if (!isUserValid(user)) {
            logger.error("Invalid user");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Check if the user already exists by its email
        if (userService.userExistsByEmail(user.getEmail())) {
            logger.error("User email already exists");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        User createdUser = userService.createUser(user);
        logger.info("User created successfully");
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found", 
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("email/{email}")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        logger.info("Attempting to get user by an email");
        if (!isEmailValid(email)) {
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
    @PutMapping("email/{email}")
    public ResponseEntity<User> updateUser(@RequestBody String email, @RequestBody User updatedUser) {
        logger.info("Attempting to update user by email");

        // Obtain the existing user
        Optional<User> existingUserOptional = userService.getUserByEmail(email);
        if (!existingUserOptional.isPresent()) {
            logger.error("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if the updated user is valid
        if (!isUserValid(updatedUser)) {
            logger.error("Invalid updatedUser");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //TODO code 403 forbidden when authentication is further done

        // Update the user
        User userToUpdate = existingUserOptional.get();
        if (userService.updateUser(userToUpdate.getId(), updatedUser) != null) {
            logger.info("User updated");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Delete user by email", description = "Delete an existing user by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteByEmail(@RequestBody String email) {
        logger.info("Attempting to delete user by email");
        if (!isEmailValid(email)) {
            logger.error("Invalid email");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // TODO code 403 forbidden when authentication is further done

        if (!userService.userExistsByEmail(email)) {
            logger.error("User not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteByEmail(email);
        logger.info("User deleted");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get users by restaurant ID", description = "Retrieve users associated with a specific restaurant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid restaurant ID"),
        @ApiResponse(responseCode = "204", description = "No users found")
    })
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<User>> getUsersByRestaurantId(@RequestParam UUID restaurantId) {
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
    public ResponseEntity<List<User>> getUsersByRole(@RequestParam Role role) {
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

    /* Helper functions */

    private boolean isUserValid(User user) {
        String fullname = user.getFullname();
        String email = user.getEmail();
        String password = user.getPassword();
        Role role = user.getRole();

        if (fullname == null || fullname.length() <= MIN_FULLNAME_LENGTH || fullname.length() >= MAX_FULLNAME_LENGTH) {
            return false;
        }

        if (password == null || password.length() <= MIN_PASSWORD_LENGTH || password.length() >= MAX_PASSWORD_LENGTH) {
            return false;
        }

        if (role == null || role != Role.DISASSOCIATED) { // Enforce that the default role is Disassociated
            return false;
        }

        return isEmailValid(email);
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        return email.length() >= MIN_EMAIL_LENGTH && email.length() <= MAX_EMAIL_LENGTH && email.matches(EMAIL_REGEX);
    }
}