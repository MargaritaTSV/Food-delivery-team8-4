package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceImplIntegrationTest {

    private OrderServiceImpl orderService;
    private CartServiceImpl cartService;
    private ClientServiceImpl clientService;
    private static Long testClientId;
    private static String testPhone;
    private static String testEmail;

    @BeforeAll
    static void setupDatabase() {
        DatabaseConnection.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
    }

    @AfterAll
    static void cleanup() {
        if (testClientId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE customer_id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl();
        clientService = new ClientServiceImpl(cartService);
        orderService = new OrderServiceImpl(cartService);
        long suffix = System.currentTimeMillis() % 10000000000L;
        testPhone = "+7" + String.format("%010d", suffix).substring(0, 10);
        testEmail = "order_test_" + System.currentTimeMillis() + "@test.com";
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Place order with CARD payment")
    void testPlaceOrderCard() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        Client client = clientService.register(testPhone, "Password123!", "Test User", testEmail, address);
        testClientId = client.getId();

        // Add items to cart
        CartItem item = CartItem.builder()
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .price(100.0)
                .build();
        cartService.addItem(testClientId, item);

        Address deliveryAddress = Address.builder()
                .country("Russia").city("Moscow").street("Delivery").building("2")
                .apartment("20").entrance("2").floor(2)
                .latitude(55.7558).longitude(37.6173).build();

        Order order = orderService.placeOrder(testClientId, deliveryAddress, PaymentMethodForOrder.CARD);
        assertNotNull(order);
        assertNotNull(order.getId());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Place order with CASH payment")
    void testPlaceOrderCash() {
        if (testClientId == null) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            Client client = clientService.register(phone, "Password123!", "Test", testEmail + "cash", address);
            testClientId = client.getId();
        }

        CartItem item = CartItem.builder()
                .productId(2L)
                .productName("Test Product 2")
                .quantity(1)
                .price(50.0)
                .build();
        cartService.addItem(testClientId, item);

        Address deliveryAddress = Address.builder()
                .country("Russia").city("Moscow").street("Delivery").building("3")
                .apartment("30").entrance("3").floor(3)
                .latitude(55.7558).longitude(37.6173).build();

        Order order = orderService.placeOrder(testClientId, deliveryAddress, PaymentMethodForOrder.CASH);
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Place order with ONLINE payment method")
    void testPlaceOrderOnline() {
        if (testClientId == null) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            Client client = clientService.register(phone, "Password123!", "Test", testEmail + "online", address);
            testClientId = client.getId();
        }

        cartService.clear(testClientId);
        CartItem item = CartItem.builder()
                .productId(3L)
                .productName("Test Product 3")
                .quantity(1)
                .price(75.0)
                .build();
        cartService.addItem(testClientId, item);

        Address deliveryAddress = Address.builder()
                .country("Russia").city("Moscow").street("Delivery").building("4")
                .apartment("40").entrance("4").floor(4)
                .latitude(55.7558).longitude(37.6173).build();

        Order order = orderService.placeOrder(testClientId, deliveryAddress, PaymentMethodForOrder.ONLINE);
        assertNotNull(order);
        assertNotNull(order.getId());
        // ONLINE обрабатывается как CARD, поэтому статус должен быть PAID
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
}

