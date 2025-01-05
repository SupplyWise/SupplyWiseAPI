package com.supplywise.supplywise.services;

import com.supplywise.supplywise.model.Notification;
import com.supplywise.supplywise.model.Restaurant;
import com.supplywise.supplywise.repositories.NotificationRepository;
import com.supplywise.supplywise.repositories.RestaurantRepository;
import com.supplywise.supplywise.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @InjectMocks
    private NotificationService notificationService;

    private UUID restaurantId;
    private UUID notificationId;
    private Notification notification;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurantId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        
        restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        
        notification = new Notification();
        notification.setId(notificationId);
        notification.setMessage("Test notification");
        notification.setRestaurant(restaurant);
    }

    @Test
    void testCreateNotification_Success() {
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.createNotification(notification);

        assertNotNull(result);
        assertEquals(notification.getId(), result.getId());
        assertEquals(notification.getMessage(), result.getMessage());
        assertEquals(notification.getRestaurant().getId(), result.getRestaurant().getId());
        verify(notificationRepository).save(notification);
        verify(notificationWebSocketHandler).sendNotification(contains("Test notification"));
    }

    @Test
    void testUpdateNotification_Success() {
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.updateNotification(notification);

        assertNotNull(result);
        assertEquals(notification.getId(), result.getId());
        assertEquals(notification.getRestaurant().getId(), result.getRestaurant().getId());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testGetActiveNotifications_WithNotifications() {
        when(notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false))
            .thenReturn(Arrays.asList(notification));

        List<Notification> results = notificationService.getActiveNotifications(restaurantId);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(notification.getId(), results.get(0).getId());
        assertEquals(notification.getRestaurant().getId(), results.get(0).getRestaurant().getId());
        verify(notificationRepository).findByRestaurantIdAndIsResolved(restaurantId, false);
    }

    @Test
    void testGetActiveNotifications_NoNotifications() {
        when(notificationRepository.findByRestaurantIdAndIsResolved(restaurantId, false))
            .thenReturn(Collections.emptyList());

        List<Notification> results = notificationService.getActiveNotifications(restaurantId);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(notificationRepository).findByRestaurantIdAndIsResolved(restaurantId, false);
    }

    @Test
    void testResolveNotification_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.resolveNotification(notificationId);

        assertTrue(notification.isResolved());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    void testResolveNotification_NotificationNotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.resolveNotification(notificationId));
        
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testReadNotification_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.readNotification(notificationId);

        assertTrue(notification.isRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    void testReadNotification_NotificationNotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.readNotification(notificationId));
        
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testUnreadNotification_Success() {
        notification.setRead(true);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.unreadNotification(notificationId);

        assertFalse(notification.isRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    void testUnreadNotification_NotificationNotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.unreadNotification(notificationId));
        
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testDeleteNotification_Success() {
        doNothing().when(notificationRepository).delete(notification);

        notificationService.deleteNotification(notification);

        verify(notificationRepository).delete(notification);
    }
}