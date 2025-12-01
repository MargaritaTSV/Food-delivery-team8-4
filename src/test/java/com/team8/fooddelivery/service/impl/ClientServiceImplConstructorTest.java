package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.service.NotificationService;
import com.team8.fooddelivery.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientServiceImplConstructorTest {

    @Test
    @DisplayName("Constructor with CartService and NotificationService should create instance")
    void testConstructorWithNotificationService() {
        CartServiceImpl cartService = new CartServiceImpl();
        NotificationService notificationService = new NotificationServiceImpl();
        ClientServiceImpl service = new ClientServiceImpl(cartService, notificationService);
        assertNotNull(service);
    }
}

