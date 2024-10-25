package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // getUserByEmail tests

    @Test
    void testGetUserByEmail_UserFound() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserByEmail(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserByEmail(email);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetUserByEmail_InvalidEmail() {
        String email = "invalid-email";

        when(userService.isEmailValid(email)).thenReturn(false);

        ResponseEntity<User> response = userController.getUserByEmail(email);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // updateUser tests

    @Test
    void testUpdateUser_UserFoundAndValid() {
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setEmail(email);

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setFullname("New Name");

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userService.isUserValid(updatedUser)).thenReturn(true);

        ResponseEntity<User> response = userController.updateUser(email, updatedUser);

        verify(userService, times(1)).isEmailValid(email);
        verify(userService, times(1)).updateUser(existingUser.getId(), updatedUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        String email = "test@example.com";
        User updatedUser = new User();
        updatedUser.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.updateUser(email, updatedUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_InvalidEmail() {
        String email = "invalid-email";
        User updatedUser = new User();
        updatedUser.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(false);

        ResponseEntity<User> response = userController.updateUser(email, updatedUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateUser_InvalidUserData() {
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setEmail(email);

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userService.isUserValid(updatedUser)).thenReturn(false);

        ResponseEntity<User> response = userController.updateUser(email, updatedUser);

        verify(userService, times(1)).isEmailValid(email);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // deleteByEmail tests

    @Test
    void testDeleteByEmail_UserFound() {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.userExistsByEmail(email)).thenReturn(true);

        ResponseEntity<Void> response = userController.deleteByEmail(email);

        verify(userService, times(1)).deleteByEmail(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteByEmail_UserNotFound() {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.userExistsByEmail(email)).thenReturn(false);

        ResponseEntity<Void> response = userController.deleteByEmail(email);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteByEmail_InvalidEmail() {
        String email = "invalid-email";

        when(userService.isEmailValid(email)).thenReturn(false);

        ResponseEntity<Void> response = userController.deleteByEmail(email);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
