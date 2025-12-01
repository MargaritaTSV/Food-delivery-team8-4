package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderRepositoryTest {

    private OrderRepository orderRepository;
    private static ClientRepository clientRepository;
    private static Long testClientId;
    private static Long testOrderId;

    @BeforeAll
    static void setup() throws SQLException {
        DatabaseConnection.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        // Создаем тестового клиента (проверяем существование)
        clientRepository = new ClientRepository();
        var existingClient = clientRepository.findByPhone("+79999999999");
        if (existingClient.isPresent()) {
            testClientId = existingClient.get().getId();
        } else {
            com.team8.fooddelivery.model.client.Client client = com.team8.fooddelivery.model.client.Client.builder()
                    .name("Test Client")
                    .phone("+79999999999")
                    .email("test" + System.currentTimeMillis() + "@test.com")
                    .passwordHash("hashed")
                    .address(Address.builder().city("Test").street("Test").building("1").build())
                    .isActive(true)
                    .build();
            testClientId = clientRepository.save(client);
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Удаляем тестовые данные в правильном порядке (из-за foreign keys)
        if (testOrderId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                stmt.setLong(1, testOrderId);
                stmt.executeUpdate();
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM payments WHERE order_id = ?")) {
                stmt.setLong(1, testOrderId);
                stmt.executeUpdate();
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
                stmt.setLong(1, testOrderId);
                stmt.executeUpdate();
            }
        }
        if (testClientId != null && clientRepository != null) {
            // Удаляем все заказы клиента перед удалением клиента
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE customer_id = ?)")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM payments WHERE order_id IN (SELECT id FROM orders WHERE customer_id = ?)")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM orders WHERE customer_id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            }
            clientRepository.delete(testClientId);
        }
    }

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Save order and return ID")
    void testSave() throws SQLException {
        Order order = new Order();
        order.setCustomerId(testClientId);
        order.setRestaurantId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(Address.builder()
                .city("Moscow")
                .street("Test")
                .building("1")
                .build());
        order.setTotalPrice(1000.0);
        order.setPaymentMethod(PaymentMethodForOrder.CARD);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(Instant.now());

        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Test Product")
                .quantity(1)
                .price(1000.0)
                .build());
        order.setItems(items);

        testOrderId = orderRepository.save(order);
        assertNotNull(testOrderId);
        assertTrue(testOrderId > 0);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Find order by ID")
    void testFindById() throws SQLException {
        if (testOrderId == null) {
            // Если заказ не был создан в предыдущем тесте, создаем его здесь
            Order order = new Order();
            order.setCustomerId(testClientId);
            order.setRestaurantId(1L);
            order.setStatus(OrderStatus.PENDING);
            order.setDeliveryAddress(Address.builder()
                    .city("Moscow")
                    .street("Test")
                    .building("1")
                    .build());
            order.setTotalPrice(1000.0);
            order.setPaymentMethod(PaymentMethodForOrder.CARD);
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setCreatedAt(Instant.now());
            List<CartItem> items = new ArrayList<>();
            items.add(CartItem.builder()
                    .productName("Test Product")
                    .quantity(1)
                    .price(1000.0)
                    .build());
            order.setItems(items);
            testOrderId = orderRepository.save(order);
        }
        Optional<Order> found = orderRepository.findById(testOrderId);
        assertTrue(found.isPresent());
        Order order = found.get();
        assertEquals(testOrderId, order.getId());
        assertEquals(testClientId, order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Find orders by customer ID")
    void testFindByCustomerId() throws SQLException {
        List<Order> orders = orderRepository.findByCustomerId(testClientId);
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(testOrderId)));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Update order")
    void testUpdate() throws SQLException {
        if (testOrderId == null) {
            // Если заказ не был создан, создаем его здесь
            Order order = new Order();
            order.setCustomerId(testClientId);
            order.setRestaurantId(1L);
            order.setStatus(OrderStatus.PENDING);
            order.setDeliveryAddress(Address.builder()
                    .city("Moscow")
                    .street("Test")
                    .building("1")
                    .build());
            order.setTotalPrice(1000.0);
            order.setPaymentMethod(PaymentMethodForOrder.CARD);
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setCreatedAt(Instant.now());
            List<CartItem> items = new ArrayList<>();
            items.add(CartItem.builder()
                    .productName("Test Product")
                    .quantity(1)
                    .price(1000.0)
                    .build());
            order.setItems(items);
            testOrderId = orderRepository.save(order);
        }
        Optional<Order> orderOpt = orderRepository.findById(testOrderId);
        assertTrue(orderOpt.isPresent());
        Order order = orderOpt.get();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalPrice(1500.0);

        orderRepository.update(order);

        Optional<Order> updated = orderRepository.findById(testOrderId);
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updated.get().getStatus());
        assertEquals(1500.0, updated.get().getTotalPrice());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("Find orders by status")
    void testFindByStatus() throws SQLException {
        List<Order> orders = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        assertNotNull(orders);
        assertTrue(orders.stream().anyMatch(o -> o.getId().equals(testOrderId)));
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("Find all orders")
    void testFindAll() throws SQLException {
        List<Order> orders = orderRepository.findAll();
        assertNotNull(orders);
        // Может быть пустым, если других заказов нет - это нормально
        // Проверяем только, что метод работает без ошибок
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("Delete order")
    void testDelete() throws SQLException {
        // Создаем временный заказ для удаления
        Order tempOrder = new Order();
        tempOrder.setCustomerId(testClientId);
        tempOrder.setRestaurantId(1L);
        tempOrder.setStatus(OrderStatus.PENDING);
        tempOrder.setDeliveryAddress(Address.builder()
                .city("Moscow")
                .street("Test")
                .building("1")
                .build());
        tempOrder.setTotalPrice(500.0);
        tempOrder.setPaymentMethod(PaymentMethodForOrder.CARD);
        tempOrder.setPaymentStatus(PaymentStatus.PENDING);
        tempOrder.setCreatedAt(Instant.now());
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder()
                .productName("Temp Product")
                .quantity(1)
                .price(500.0)
                .build());
        tempOrder.setItems(items);

        Long tempId = orderRepository.save(tempOrder);
        assertNotNull(tempId);

        // Удаляем
        orderRepository.delete(tempId);

        // Проверяем, что удален
        Optional<Order> deleted = orderRepository.findById(tempId);
        assertFalse(deleted.isPresent());
    }
}

