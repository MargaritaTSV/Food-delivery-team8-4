package com.team8.fooddelivery.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationBuilderTest {

    @Test
    @DisplayName("NotificationBuilder toString should be callable")
    void testNotificationBuilderToString() {
        Notification.NotificationBuilder builder = Notification.builder()
                .id(1L)
                .clientId(100L)
                .type("TEST")
                .message("Test message")
                .timestamp(LocalDateTime.now());

        // Call toString to cover the method
        String result = builder.toString();
        assertNotNull(result);
    }

    @Test
    @DisplayName("NotificationBuilder should build notification correctly")
    void testNotificationBuilderBuild() {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = Notification.builder()
                .id(1L)
                .clientId(100L)
                .type("TEST")
                .message("Test message")
                .timestamp(now)
                .build();

        assertNotNull(notification);
        assertEquals(1L, notification.getId());
        assertEquals(100L, notification.getClientId());
        assertEquals("TEST", notification.getType());
        assertEquals("Test message", notification.getMessage());
        assertEquals(now, notification.getTimestamp());
    }
}

