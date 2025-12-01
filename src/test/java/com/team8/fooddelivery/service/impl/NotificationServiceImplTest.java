package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.notification.Notification;
import com.team8.fooddelivery.model.notification.NotificationTemplate;
import com.team8.fooddelivery.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceImplTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl();
    }

    @Test
    void testNotify() {
        Long clientId = 1L;
        notificationService.notify(clientId, NotificationTemplate.WELCOME_ACCOUNT, "Иван");

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        // WELCOME_ACCOUNT template has type "ACCOUNT", not "WELCOME"
        assertEquals("ACCOUNT", notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("Иван"));
    }

    @Test
    void testNotifyAccount() {
        Long clientId = 2L;
        notificationService.notifyAccount(clientId, "Профиль обновлен");

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        assertEquals("ACCOUNT", notifications.get(0).getType());
    }

    @Test
    void testNotifyWelcome() {
        Long clientId = 3L;
        notificationService.notifyWelcome(clientId, "Мария");

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        // WELCOME_ACCOUNT template has type "ACCOUNT", not "WELCOME"
        assertEquals("ACCOUNT", notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("Мария"));
    }

    @Test
    void testNotifyOrderPlaced() {
        Long clientId = 4L;
        notificationService.notifyOrderPlaced(clientId, 101L, 1500L);

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        assertEquals("ORDER", notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("101"));
        assertTrue(notifications.get(0).getMessage().contains("1500"));
    }

    @Test
    void testNotifyOrderPaid() {
        Long clientId = 5L;
        notificationService.notifyOrderPaid(clientId, 102L);

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        assertEquals("ORDER", notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("102"));
    }

    @Test
    void testNotifyDelivery() {
        Long clientId = 6L;
        notificationService.notifyDelivery(clientId, 103L);

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(1, notifications.size());
        // ORDER_DELIVERED template has type "ORDER", not "DELIVERY"
        assertEquals("ORDER", notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("103"));
    }

    @Test
    void testGetNotificationsEmpty() {
        List<Notification> notifications = notificationService.getNotifications(999L);
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testGetNotificationsMultiple() {
        Long clientId = 7L;
        notificationService.notifyWelcome(clientId, "Тест");
        notificationService.notifyOrderPlaced(clientId, 1L, 100L);
        notificationService.notifyOrderPaid(clientId, 1L);

        List<Notification> notifications = notificationService.getNotifications(clientId);
        assertEquals(3, notifications.size());
    }

    @Test
    void testClear() {
        Long clientId = 8L;
        notificationService.notifyWelcome(clientId, "Тест");
        notificationService.notifyOrderPlaced(clientId, 1L, 100L);

        List<Notification> beforeClear = notificationService.getNotifications(clientId);
        assertEquals(2, beforeClear.size());

        notificationService.clear(clientId);

        List<Notification> afterClear = notificationService.getNotifications(clientId);
        assertTrue(afterClear.isEmpty());
    }

    @Test
    void testNotificationIdSequence() {
        Long clientId1 = 10L;
        Long clientId2 = 11L;

        notificationService.notifyWelcome(clientId1, "Тест1");
        notificationService.notifyWelcome(clientId2, "Тест2");
        notificationService.notifyWelcome(clientId1, "Тест3");

        List<Notification> notifications1 = notificationService.getNotifications(clientId1);
        List<Notification> notifications2 = notificationService.getNotifications(clientId2);

        assertEquals(2, notifications1.size());
        assertEquals(1, notifications2.size());

        // IDs should be sequential
        assertTrue(notifications1.get(0).getId() < notifications1.get(1).getId());
        assertTrue(notifications1.get(1).getId() > notifications2.get(0).getId());
    }
}

