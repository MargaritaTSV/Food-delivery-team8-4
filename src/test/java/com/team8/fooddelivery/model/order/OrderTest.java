package com.team8.fooddelivery.model.order;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.model.product.CartItem;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testOrderCreation() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerId(100L);
        order.setRestaurantId(200L);
        order.setDeliveryAddressId(300L);
        order.setCourierId(400L);
        order.setTotalPrice(1500.0);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(PaymentMethodForOrder.CARD);
        order.setCreatedAt(Instant.now());
        order.setEstimatedDeliveryTime(Instant.now().plusSeconds(3600));

        assertEquals(1L, order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(100L, order.getCustomerId());
        assertEquals(200L, order.getRestaurantId());
        assertEquals(300L, order.getDeliveryAddressId());
        assertEquals(400L, order.getCourierId());
        assertEquals(1500.0, order.getTotalPrice());
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        assertEquals(PaymentMethodForOrder.CARD, order.getPaymentMethod());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getEstimatedDeliveryTime());
    }

    @Test
    void testOrderWithItems() {
        Order order = new Order();
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Пицца")
                .quantity(2)
                .price(500.0)
                .build());
        items.add(CartItem.builder()
                .productName("Кола")
                .quantity(1)
                .price(200.0)
                .build());
        order.setItems(items);

        assertEquals(2, order.getItems().size());
        assertEquals("Пицца", order.getItems().get(0).getProductName());
        assertEquals(2, order.getItems().get(0).getQuantity());
        assertEquals(500.0, order.getItems().get(0).getPrice());
    }

    @Test
    void testOrderWithAddress() {
        Order order = new Order();
        Address address = Address.builder()
                .city("Москва")
                .street("Ленина")
                .building("10")
                .build();
        order.setDeliveryAddress(address);

        assertNotNull(order.getDeliveryAddress());
        assertEquals("Москва", order.getDeliveryAddress().getCity());
        assertEquals("Ленина", order.getDeliveryAddress().getStreet());
        assertEquals("10", order.getDeliveryAddress().getBuilding());
    }

    @Test
    void testTestOrders() {
        assertNotNull(Order.TEST_ORDERS);
        assertFalse(Order.TEST_ORDERS.isEmpty());
        assertTrue(Order.TEST_ORDERS.containsKey(101L));
        assertTrue(Order.TEST_ORDERS.containsKey(102L));

        Order o1 = Order.TEST_ORDERS.get(101L);
        assertNotNull(o1);
        assertEquals(OrderStatus.READY_FOR_PICKUP, o1.getStatus());
        assertEquals(1500.0, o1.getTotalPrice());

        Order o2 = Order.TEST_ORDERS.get(102L);
        assertNotNull(o2);
        assertEquals(OrderStatus.PENDING, o2.getStatus());
        assertEquals(900.0, o2.getTotalPrice());
    }

    @Test
    void testOrderStatusTransitions() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        assertEquals(OrderStatus.PENDING, order.getStatus());

        order.setStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        order.setStatus(OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setId(1L);
        order2.setStatus(OrderStatus.PENDING);

        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void testToString() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        assertNotNull(order.toString());
    }
}

