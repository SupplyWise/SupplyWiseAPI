package com.supplywise.supplywise.controllers;

import com.supplywise.supplywise.model.User;
import com.supplywise.supplywise.DAO.CreateUserRequest;
import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.model.Role;
import com.supplywise.supplywise.services.JwtService;
import com.supplywise.supplywise.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfiguration.class) //Ensure that the security configuration is loaded - DO NOT REMOVE
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_Success() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        User user = new User();
        user.setFullname("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(Role.DISASSOCIATED);

        // Mock the user creation
        when(userService.isUserValid(any(User.class))).thenReturn(true);
        when(userService.userExistsByEmail(anyString())).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Make the request and check if it returns CREATED (201)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullname").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testCreateUser_InvalidUser() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest("John Doe", "invalid_email", "password123");

        // Simulate that the user is invalid
        when(userService.isUserValid(any(User.class))).thenReturn(false);

        // Make the request and check if it returns BAD_REQUEST (400)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        String email = "john@example.com";
        String password = "password123";
        UserDetails userDetails = mock(UserDetails.class);

        // Mock an existing user with the correct password
        when(userService.userExistsByEmail(email)).thenReturn(true);
        when(userService.loadUserByEmail(email)).thenReturn(userDetails);
        when(userService.isPasswordCorrect(password, userDetails.getPassword())).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("mock-jwt-token");

        // Make the request and check if it returns OK (200)
        mockMvc.perform(post("/api/auth/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        String email = "notfound@example.com";
        String password = "password123";

        // Mock a user that does not exist
        when(userService.userExistsByEmail(email)).thenReturn(false);

        // Make the request and check if it returns NOT_FOUND (404)
        mockMvc.perform(post("/api/auth/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginUser_InvalidPassword() throws Exception {
        String email = "john@example.com";
        String password = "wrongpassword";
        UserDetails userDetails = mock(UserDetails.class);

        // Mock an existing user with a different password
        when(userService.userExistsByEmail(email)).thenReturn(true);
        when(userService.loadUserByEmail(email)).thenReturn(userDetails);
        when(userService.isPasswordCorrect(password, userDetails.getPassword())).thenReturn(false);

        // Make the request and check if it returns BAD_REQUEST (400)
        mockMvc.perform(post("/api/auth/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isBadRequest());
    }
}
