package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.UserService;

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
public class UserController{

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    // create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User createdUser = userService.createUser(user);
        logger.info("User created: {}", createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    
    // get user by id
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id){
        Optional<User> userOptional = userService.getUserById(id);
        if(userOptional.isPresent()){
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username){
        Optional<User> userOptional = userService.getUserByUsername(username);
        if(userOptional.isPresent()){
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // get users by username containing a part of the username
    @GetMapping("/username/contains/{usernamePart}")
    public ResponseEntity<List<User>> getUsersByUsernameContaining(@PathVariable String usernamePart){
        List<User> users = userService.getUsersByUsernameContaining(usernamePart);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role){
        List<User> users = userService.getUsersByRole(role);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // get users by restaurant id
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<User>> getUsersByRestaurantId(@PathVariable UUID restaurantId){
        List<User> users = userService.getUsersByRestaurantId(restaurantId);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // get users by role and restaurant id
    @GetMapping("/role/{role}/restaurant/{restaurant}")
    public ResponseEntity<List<User>> getUsersByRoleAndRestaurantId(@PathVariable Role role, @PathVariable UUID restaurant){
        List<User> users = userService.getUsersByRoleAndRestaurantId(role, restaurant);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // delete user
    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username){
        if(!userService.existsByUsername(username)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteByUsername(username);
        logger.info("User deleted: {}", username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    

    // confirm if user exists by its username
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username){
        boolean exists = userService.existsByUsername(username);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    // confirm if user exists by its id
    @GetMapping("/exists/id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable UUID id){
        boolean exists = userService.existsById(id);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
    
    // get all users
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable){
        Page<User> users = userService.getAllUsers(pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}