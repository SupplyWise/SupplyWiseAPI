package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.model.Role;
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

    // create a new user
    public User createUser(User user){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // update an existing user
    public Optional<User> updateUser(UUID id, org.apache.catalina.User updatedUser){
        if (!userRepository.existsById(id)) {
            return Optional.empty();
        }
        User existingUser = userRepository.findById(id).get();

        existingUser.setUsername(updatedUser.getUsername());
        if(!updatedUser.getPassword().equals(existingUser.getPassword())){
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existingUser.setRole(updatedUser.getRole());
        existingUser.setRestaurant(updatedUser.getRestaurant());

        return Optional.of(userRepository.save(existingUser));
    }

    // get user by id
    public Optional<User> getUserById(UUID id){
        return userRepository.findById(id);
    }

    // get user by username
    public Optional<User> getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    // get users by username containing a part of the username
    public List<User> getUsersByUsernameContaining(String usernamePart){
        return userRepository.findByUsernameContaining(usernamePart);
    }

    // get users by role
    public List<User> getUsersByRole(Role role){
        return userRepository.findByRole(role);
    }

    // get users by restaurant id
    public List<User> getUsersByRestaurantId(UUID restaurantId) {
        return userRepository.findByRestaurantId(restaurantId);
    }

    // get users by role and restaurant id
    public List<User> getUsersByRoleAndRestaurantId(Role role, UUID restaurantId){
        return userRepository.findByRoleAndRestaurantId(role, restaurantId);
    }

    // delete user
    public void deleteByUsername(String username){
        userRepository.deleteByUsername(username);
    }

    // confirm if user exists by its username
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    // confirm if user exists by its id
    public boolean existsById(UUID id){
        return userRepository.existsById(id);
    }

    // get all users
    public Page<User> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable);
    }
}