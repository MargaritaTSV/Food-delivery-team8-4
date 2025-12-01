package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.repository.ClientRepository;
import com.team8.fooddelivery.repository.OrderRepository;
import com.team8.fooddelivery.repository.PaymentRepository;
import com.team8.fooddelivery.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrderServiceImplErrorHandlingTest {

    @Mock
    private CartServiceImpl cartService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Long clientId = 1L;
    private Address deliveryAddress;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        deliveryAddress = Address.builder()
                .country("Country").city("City").street("Street").building("1").build();
        
        // Создаем OrderServiceImpl с моками через рефлексию
        orderService = new OrderServiceImpl(cartService, notificationService);
        
        // Используем рефлексию для установки моков
        java.lang.reflect.Field orderRepoField = OrderServiceImpl.class.getDeclaredField("orderRepository");
        orderRepoField.setAccessible(true);
        orderRepoField.set(orderService, orderRepository);
        
        java.lang.reflect.Field paymentRepoField = OrderServiceImpl.class.getDeclaredField("paymentRepository");
        paymentRepoField.setAccessible(true);
        paymentRepoField.set(orderService, paymentRepository);
        
        java.lang.reflect.Field clientRepoField = OrderServiceImpl.class.getDeclaredField("clientRepository");
        clientRepoField.setAccessible(true);
        clientRepoField.set(orderService, clientRepository);
    }

    @Test
    @DisplayName("Place Order: Client not found should throw IllegalArgumentException")
    void testPlaceOrder_ClientNotFound() throws SQLException {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // OrderServiceImpl использует orElseThrow, который выбрасывает IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD));

        verify(clientRepository, times(1)).findById(clientId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place Order: Inactive client should throw IllegalStateException")
    void testPlaceOrder_InactiveClient() throws SQLException {
        Client inactiveClient = Client.builder()
                .id(clientId)
                .isActive(false)
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(inactiveClient));

        assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD));

        verify(clientRepository, times(1)).findById(clientId);
        verify(cartService, never()).getCartForClient(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place Order: Cart not found should throw IllegalStateException")
    void testPlaceOrder_CartNotFound() throws SQLException {
        Client activeClient = Client.builder()
                .id(clientId)
                .isActive(true)
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
        when(cartService.getCartForClient(clientId)).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD));

        verify(clientRepository, times(1)).findById(clientId);
        verify(cartService, times(1)).getCartForClient(clientId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place Order: Empty cart should throw IllegalStateException")
    void testPlaceOrder_EmptyCart() throws SQLException {
        Client activeClient = Client.builder()
                .id(clientId)
                .isActive(true)
                .build();

        Cart emptyCart = new Cart();
        emptyCart.setItems(new ArrayList<>());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
        when(cartService.getCartForClient(clientId)).thenReturn(emptyCart);

        assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD));

        verify(clientRepository, times(1)).findById(clientId);
        verify(cartService, times(1)).getCartForClient(clientId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place Order: SQLException during save should throw RuntimeException")
    void testPlaceOrder_SQLExceptionOnSave() throws SQLException {
        Client activeClient = Client.builder()
                .id(clientId)
                .isActive(true)
                .build();

        Cart cart = new Cart();
        cart.setItems(List.of(
                CartItem.builder().productName("Item").quantity(1).price(10.0).build()
        ));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
        when(cartService.getCartForClient(clientId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenThrow(new SQLException("DB Error"));

        assertThrows(RuntimeException.class, () ->
                orderService.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Place Order: Payment with PENDING status for non-CASH should set order to CANCELLED")
    void testPlaceOrder_PendingPaymentForNonCash() throws Exception {
        Client activeClient = Client.builder()
                .id(clientId)
                .isActive(true)
                .build();

        Cart cart = new Cart();
        cart.setItems(List.of(
                CartItem.builder().productName("Item").quantity(1).price(10.0).build()
        ));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(activeClient));
        when(cartService.getCartForClient(clientId)).thenReturn(cart);
        
        Order savedOrder = new Order();
        savedOrder.setId(100L);
        when(orderRepository.save(any(Order.class))).thenReturn(100L);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(savedOrder));
        
        // Создаем OrderServiceImpl с моками через рефлексию
        OrderServiceImpl orderServiceWithMocks = new OrderServiceImpl(cartService, notificationService);
        
        // Используем рефлексию для установки моков
        java.lang.reflect.Field orderRepoField = OrderServiceImpl.class.getDeclaredField("orderRepository");
        orderRepoField.setAccessible(true);
        orderRepoField.set(orderServiceWithMocks, orderRepository);
        
        java.lang.reflect.Field paymentRepoField = OrderServiceImpl.class.getDeclaredField("paymentRepository");
        paymentRepoField.setAccessible(true);
        paymentRepoField.set(orderServiceWithMocks, paymentRepository);
        
        java.lang.reflect.Field clientRepoField = OrderServiceImpl.class.getDeclaredField("clientRepository");
        clientRepoField.setAccessible(true);
        clientRepoField.set(orderServiceWithMocks, clientRepository);
        
        // Мокируем paymentRepository.save чтобы изменить статус payment на PENDING для CARD
        // Это симулирует ситуацию, когда payment после сохранения имеет PENDING статус
        when(paymentRepository.save(any(com.team8.fooddelivery.model.client.Payment.class))).thenAnswer(invocation -> {
            com.team8.fooddelivery.model.client.Payment p = invocation.getArgument(0);
            // Для CARD метода меняем статус на PENDING (необычная ситуация, но возможная)
            // Это позволит покрыть строку 98 (CANCELLED для PENDING не-CASH)
            if (p.getMethod() == PaymentMethodForOrder.CARD) {
                p.setStatus(PaymentStatus.PENDING);
            }
            return 1L;
        });
        
        // Вызываем placeOrder с CARD методом
        // processPayment вернет SUCCESS, но мы мокируем save чтобы изменить статус на PENDING
        // После save мы проверяем payment.getStatus(), который теперь будет PENDING
        // Это должно покрыть строку 98 (CANCELLED для PENDING не-CASH)
        Order result = orderServiceWithMocks.placeOrder(clientId, deliveryAddress, PaymentMethodForOrder.CARD);
        assertNotNull(result);
        // Проверяем, что заказ был отменен из-за PENDING статуса для не-CASH метода
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }
}

