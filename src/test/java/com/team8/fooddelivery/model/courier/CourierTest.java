package com.team8.fooddelivery.model.courier;

import com.team8.fooddelivery.model.notification.Notification;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourierTest {

    @Test
    void testCourierCreation() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setName("Иван Иванов");
        courier.setPassword("password123");
        courier.setPhoneNumber("+79991234567");
        courier.setStatus(CourierStatus.ON_SHIFT);
        courier.setTransportType("bike");
        courier.setCurrentOrderId(100L);
        courier.setCurrentBalance(500.0);
        courier.setBankCard(1234567890123456L);

        assertEquals(1L, courier.getId());
        assertEquals("Иван Иванов", courier.getName());
        assertEquals("password123", courier.getPassword());
        assertEquals("+79991234567", courier.getPhoneNumber());
        assertEquals(CourierStatus.ON_SHIFT, courier.getStatus());
        assertEquals("bike", courier.getTransportType());
        assertEquals(100L, courier.getCurrentOrderId());
        assertEquals(500.0, courier.getCurrentBalance());
        assertEquals(1234567890123456L, courier.getBankCard());
    }

    @Test
    void testCourierWithNotifications() {
        Courier courier = new Courier();
        List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.builder()
                .id(1L)
                .clientId(1L)
                .type("ORDER")
                .message("New order")
                .build());
        courier.setNotifications(notifications);

        assertNotNull(courier.getNotifications());
        assertEquals(1, courier.getNotifications().size());
        assertEquals("New order", courier.getNotifications().get(0).getMessage());
    }

    @Test
    void testTestCouriers() {
        assertNotNull(Courier.TEST_COURIERS);
        assertFalse(Courier.TEST_COURIERS.isEmpty());
        assertTrue(Courier.TEST_COURIERS.containsKey(1L));
        assertTrue(Courier.TEST_COURIERS.containsKey(2L));

        Courier c1 = Courier.TEST_COURIERS.get(1L);
        assertNotNull(c1);
        assertEquals("Ivan Petrov", c1.getName());
        assertEquals(CourierStatus.ON_SHIFT, c1.getStatus());
        assertEquals("bike", c1.getTransportType());
        assertEquals(500.0, c1.getCurrentBalance());

        Courier c2 = Courier.TEST_COURIERS.get(2L);
        assertNotNull(c2);
        assertEquals("Anna Ivanova", c2.getName());
        assertEquals(CourierStatus.DELIVERING, c2.getStatus());
        assertEquals("car", c2.getTransportType());
        assertEquals(300.0, c2.getCurrentBalance());
    }

    @Test
    void testCourierStatusTransitions() {
        Courier courier = new Courier();
        courier.setStatus(CourierStatus.OFF_SHIFT);
        assertEquals(CourierStatus.OFF_SHIFT, courier.getStatus());

        courier.setStatus(CourierStatus.ON_SHIFT);
        assertEquals(CourierStatus.ON_SHIFT, courier.getStatus());

        courier.setStatus(CourierStatus.DELIVERING);
        assertEquals(CourierStatus.DELIVERING, courier.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Courier courier1 = new Courier();
        courier1.setId(1L);
        courier1.setName("Test Courier");

        Courier courier2 = new Courier();
        courier2.setId(1L);
        courier2.setName("Test Courier");

        assertEquals(courier1, courier2);
        assertEquals(courier1.hashCode(), courier2.hashCode());
    }

    @Test
    void testToString() {
        Courier courier = new Courier();
        courier.setId(1L);
        courier.setName("Test Courier");

        assertNotNull(courier.toString());
    }
}

