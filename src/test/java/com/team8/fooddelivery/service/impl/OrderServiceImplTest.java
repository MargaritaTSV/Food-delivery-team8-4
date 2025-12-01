package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderServiceImpl orderService;
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl();
        orderService = new OrderServiceImpl(cartService);
    }

    @Test
    void testConstructor() {
        assertNotNull(orderService);
    }

    @Test
    void testConstructorWithNotificationService() {
        NotificationService notificationService = new NotificationServiceImpl();
        OrderServiceImpl service = new OrderServiceImpl(cartService, notificationService);
        assertNotNull(service);
    }

    @Test
    void testProcessPayment_Cash() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("processPayment", 
            com.team8.fooddelivery.model.order.Order.class);
        method.setAccessible(true);
        
        com.team8.fooddelivery.model.order.Order order = new com.team8.fooddelivery.model.order.Order();
        order.setId(1L);
        order.setTotalPrice(100.0);
        order.setPaymentMethod(PaymentMethodForOrder.CASH);
        
        com.team8.fooddelivery.model.client.Payment payment = 
            (com.team8.fooddelivery.model.client.Payment) method.invoke(orderService, order);
        
        assertNotNull(payment);
        assertEquals(com.team8.fooddelivery.model.client.PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethodForOrder.CASH, payment.getMethod());
    }

    @Test
    void testProcessPayment_Online() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("processPayment", 
            com.team8.fooddelivery.model.order.Order.class);
        method.setAccessible(true);
        
        com.team8.fooddelivery.model.order.Order order = new com.team8.fooddelivery.model.order.Order();
        order.setId(1L);
        order.setTotalPrice(100.0);
        order.setPaymentMethod(PaymentMethodForOrder.ONLINE);
        
        com.team8.fooddelivery.model.client.Payment payment = 
            (com.team8.fooddelivery.model.client.Payment) method.invoke(orderService, order);
        
        assertNotNull(payment);
        assertEquals(com.team8.fooddelivery.model.client.PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(PaymentMethodForOrder.ONLINE, payment.getMethod());
    }

    @Test
    void testValidateCartItemsMethodExists() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("validateCartItems", List.class);
        assertNotNull(method);
        method.setAccessible(true);
    }

    @Test
    void testCalculateTotalMethodExists() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("calculateTotal", List.class);
        assertNotNull(method);
        method.setAccessible(true);
    }

    @Test
    void testProcessPaymentMethodExists() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("processPayment", 
            com.team8.fooddelivery.model.order.Order.class);
        assertNotNull(method);
        method.setAccessible(true);
    }

    @Test
    void testValidateCartItems_InvalidQuantity() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("validateCartItems", List.class);
        method.setAccessible(true);
        
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Test")
                .quantity(0)
                .price(100.0)
                .build());
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(orderService, items);
        });
        // Проверяем, что внутри InvocationTargetException есть IllegalArgumentException
        if (exception instanceof java.lang.reflect.InvocationTargetException) {
            Throwable cause = ((java.lang.reflect.InvocationTargetException) exception).getTargetException();
            assertTrue(cause instanceof IllegalArgumentException, "Should throw IllegalArgumentException");
        } else {
            assertTrue(exception instanceof IllegalArgumentException);
        }
    }

    @Test
    void testValidateCartItems_InvalidPrice() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("validateCartItems", List.class);
        method.setAccessible(true);
        
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Test")
                .quantity(1)
                .price(-10.0)
                .build());
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(orderService, items);
        });
        // Проверяем, что внутри InvocationTargetException есть IllegalArgumentException
        if (exception instanceof java.lang.reflect.InvocationTargetException) {
            Throwable cause = ((java.lang.reflect.InvocationTargetException) exception).getTargetException();
            assertTrue(cause instanceof IllegalArgumentException, "Should throw IllegalArgumentException");
        } else {
            assertTrue(exception instanceof IllegalArgumentException);
        }
    }

    @Test
    void testValidateCartItems_EmptyProductName() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("validateCartItems", List.class);
        method.setAccessible(true);
        
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("")
                .quantity(1)
                .price(100.0)
                .build());
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(orderService, items);
        });
        // Проверяем, что внутри InvocationTargetException есть IllegalArgumentException
        if (exception instanceof java.lang.reflect.InvocationTargetException) {
            Throwable cause = ((java.lang.reflect.InvocationTargetException) exception).getTargetException();
            assertTrue(cause instanceof IllegalArgumentException, "Should throw IllegalArgumentException");
        } else {
            assertTrue(exception instanceof IllegalArgumentException);
        }
    }

    @Test
    void testCalculateTotal() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("calculateTotal", List.class);
        method.setAccessible(true);
        
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Item1")
                .quantity(2)
                .price(100.0)
                .build());
        items.add(CartItem.builder()
                .productName("Item2")
                .quantity(1)
                .price(50.0)
                .build());
        
        Double total = (Double) method.invoke(orderService, items);
        assertEquals(250.0, total, 0.01);
    }
}

