package com.supplywise.supplywise.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.services.NotificationService;
import com.supplywise.supplywise.services.AuthHandler;
import com.supplywise.supplywise.config.SecurityConfiguration;
import com.supplywise.supplywise.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthHandler authHandler;

    private ObjectMapper objectMapper;
    private UUID restaurantId;
    private UUID notificationId;
    private Notification notification;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        restaurantId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        
        restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        
        notification = new Notification();
        notification.setId(notificationId);
        notification.setMessage("Test notification");
        notification.setRestaurant(restaurant);
        
        when(authHandler.getAuthenticatedRestaurantId()).thenReturn(restaurantId.toString());
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testGetNotifications_WithNotifications() throws Exception {
        when(notificationService.getActiveNotifications(any(UUID.class)))
            .thenReturn(Arrays.asList(notification));

        mockMvc.perform(get("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(notificationId.toString()))
            .andExpect(jsonPath("$[0].restaurant.id").value(restaurantId.toString()))
            .andExpect(jsonPath("$[0].message").value("Test notification"));

        verify(notificationService).getActiveNotifications(restaurantId);
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testGetNotifications_NoNotifications() throws Exception {
        when(notificationService.getActiveNotifications(any(UUID.class)))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(notificationService).getActiveNotifications(restaurantId);
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testGetNotificationsForRestaurant() throws Exception {
        when(notificationService.getActiveNotifications(restaurantId))
            .thenReturn(Arrays.asList(notification));

        mockMvc.perform(get("/api/notifications/{restaurantId}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(notificationId.toString()))
            .andExpect(jsonPath("$[0].restaurant.id").value(restaurantId.toString()));

        verify(notificationService).getActiveNotifications(restaurantId);
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testResolveNotification() throws Exception {
        doNothing().when(notificationService).resolveNotification(notificationId);

        mockMvc.perform(post("/api/notifications/{notificationId}/resolve", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService).resolveNotification(notificationId);
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testReadNotification() throws Exception {
        doNothing().when(notificationService).readNotification(notificationId);

        mockMvc.perform(post("/api/notifications/{notificationId}/read", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService).readNotification(notificationId);
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testUnreadNotification() throws Exception {
        doNothing().when(notificationService).unreadNotification(notificationId);

        mockMvc.perform(post("/api/notifications/{notificationId}/unread", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService).unreadNotification(notificationId);
    }
}
