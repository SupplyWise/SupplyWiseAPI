package com.supplywise.supplywise.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationWebSocketHandlerTest {

    private NotificationWebSocketHandler webSocketHandler;

    @BeforeEach
    void setUp() {
        webSocketHandler = new NotificationWebSocketHandler();
    }

    @Test
    void testAfterConnectionEstablished_AddsSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        webSocketHandler.afterConnectionEstablished(session);

        CopyOnWriteArraySet<WebSocketSession> sessions = getSessions(webSocketHandler);
        assertTrue(sessions.contains(session), "Session should be added after connection is established");
    }

    @Test
    void testAfterConnectionClosed_RemovesSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        webSocketHandler.afterConnectionEstablished(session);
        webSocketHandler.afterConnectionClosed(session, null);

        CopyOnWriteArraySet<WebSocketSession> sessions = getSessions(webSocketHandler);
        assertFalse(sessions.contains(session), "Session should be removed after connection is closed");
    }

    @Test
    void testSendNotification_SendsMessageToOpenSessions() throws IOException {
        WebSocketSession openSession = mock(WebSocketSession.class);
        WebSocketSession closedSession = mock(WebSocketSession.class);

        when(openSession.isOpen()).thenReturn(true);
        when(closedSession.isOpen()).thenReturn(false);

        webSocketHandler.afterConnectionEstablished(openSession);
        webSocketHandler.afterConnectionEstablished(closedSession);

        String message = "Test Notification";
        webSocketHandler.sendNotification(message);

        verify(openSession, times(1)).sendMessage(new TextMessage(message));
        verify(closedSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSendNotification_HandleIOException() throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        doThrow(new IOException("Simulated IO Exception")).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.afterConnectionEstablished(session);
        String message = "Test Notification";

        assertDoesNotThrow(() -> webSocketHandler.sendNotification(message), "IOException should not propagate");
    }

    // Reflection helper to access private `sessions` field
    @SuppressWarnings("unchecked")
    private CopyOnWriteArraySet<WebSocketSession> getSessions(NotificationWebSocketHandler handler) {
        try {
            var field = NotificationWebSocketHandler.class.getDeclaredField("sessions");
            field.setAccessible(true);
            return (CopyOnWriteArraySet<WebSocketSession>) field.get(handler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to access sessions field for testing", e);
        }
    }
}