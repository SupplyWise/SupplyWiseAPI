package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.services.JwtService;
import com.supplywise.supplywise.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.Optional;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class) // Ensure that security configuration is loaded - DO NOT REMOVE
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthHandler authHandler;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // getUserByEmail tests
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUserByEmail_UserFound() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUserByEmail_UserNotFound() throws Exception {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetUserByEmail_InvalidEmail() throws Exception {
        String email = "invalid-email";

        when(userService.isEmailValid(email)).thenReturn(false);

        mockMvc.perform(get("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // updateUser tests
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_UserFoundAndValid() throws Exception {
        String email = "test@example.com";
        User existingUser = new User();
        existingUser.setEmail(email);

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setFullname("New Name");

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userService.isUserValid(updatedUser)).thenReturn(true);

        mockMvc.perform(put("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"new@example.com\", \"fullname\": \"New Name\" }"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testUpdateUser_UserNotFound() throws Exception {
        String email = "test@example.com";
        User updatedUser = new User();
        updatedUser.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"new@example.com\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testUpdateUser_InvalidEmail() throws Exception {
        String email = "invalid-email";
        User updatedUser = new User();
        updatedUser.setEmail(email);

        when(userService.isEmailValid(email)).thenReturn(false);

        mockMvc.perform(put("/api/users/email/{email}", email)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"new@example.com\" }"))
                .andExpect(status().isBadRequest());
    }

    // deleteByEmail tests
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteByEmail_UserFound() throws Exception {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.userExistsByEmail(email)).thenReturn(true);

        mockMvc.perform(delete("/api/users/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testDeleteByEmail_UserNotFound() throws Exception {
        String email = "test@example.com";

        when(userService.isEmailValid(email)).thenReturn(true);
        when(userService.userExistsByEmail(email)).thenReturn(false);

        mockMvc.perform(delete("/api/users/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testDeleteByEmail_InvalidEmail() throws Exception {
        String email = "invalid-email";

        when(userService.isEmailValid(email)).thenReturn(false);

        mockMvc.perform(delete("/api/users/{email}", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
