package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.client.Payment;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import com.team8.fooddelivery.util.PasswordUtils;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PaymentRepository to achieve 100% line coverage.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentRepositoryTest {

    private PaymentRepository paymentRepository;
    private static OrderRepository orderRepository;
    private static ClientRepository clientRepository;
    private static AddressRepository addressRepository;
    
    private static Long testClientId;
    private static Long testAddressId;
    private static Long testOrderId;
    private static Long testPaymentId;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        DatabaseConnection.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        // Initialize repositories first
        orderRepository = new OrderRepository();
        clientRepository = new ClientRepository();
        addressRepository = new AddressRepository();
        
        // Create test client and address
        testClientId = createDummyClient();
        testAddressId = createDummyAddress();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Clean up in correct order due to foreign keys
        if (testPaymentId != null) {
            deletePayment(testPaymentId);
        }
        // Удаляем все заказы клиента перед удалением клиента
        if (testClientId != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Удаляем payments для заказов клиента
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM payments WHERE order_id IN (SELECT id FROM orders WHERE customer_id = ?)")) {
                    stmt.setLong(1, testClientId);
                    stmt.executeUpdate();
                }
                // Удаляем order_items для заказов клиента
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE customer_id = ?)")) {
                    stmt.setLong(1, testClientId);
                    stmt.executeUpdate();
                }
                // Удаляем заказы клиента
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE customer_id = ?")) {
                    stmt.setLong(1, testClientId);
                    stmt.executeUpdate();
                }
                // Удаляем корзины клиента
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE client_id = ?)")) {
                    stmt.setLong(1, testClientId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
                    stmt.setLong(1, testClientId);
                    stmt.executeUpdate();
                }
            }
        }
        if (testOrderId != null) {
            deleteOrder(testOrderId);
        }
        if (testAddressId != null) {
            deleteAddress(testAddressId);
        }
        if (testClientId != null) {
            deleteClient(testClientId);
        }
    }

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
    }

    // Helper methods
    private static Long createDummyClient() throws SQLException {
        Client client = Client.builder()
                .name("Payment Test Client")
                .phone("+7999" + (System.currentTimeMillis() % 10000000))
                .passwordHash(PasswordUtils.hashPassword("Password123!"))
                .email("payment_test_" + System.currentTimeMillis() + "@test.com")
                .status(ClientStatus.ACTIVE)
                .isActive(true)
                .createdAt(Instant.now())
                .orderHistory(new ArrayList<>())
                .build();
        return clientRepository.save(client);
    }

    private static Long createDummyAddress() throws SQLException {
        Address address = Address.builder()
                .country("Russia")
                .city("Moscow")
                .street("Test Street")
                .building("1")
                .apartment("10")
                .entrance("1")
                .floor(1)
                .latitude(55.7558)
                .longitude(37.6173)
                .build();
        return addressRepository.save(address);
    }

    private static Long createDummyOrder(Long clientId, Long addressId) throws SQLException {
        // Create order directly via SQL with all required columns
        // Check what columns exist in orders table
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Try with status column first
            try (PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO orders (status, customer_id, restaurant_id, delivery_address_id, delivery_address, total_price, payment_status, payment_method, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
                stmt.setString(1, OrderStatus.PENDING.name());
                stmt.setLong(2, clientId != null ? clientId : 1L);
                stmt.setLong(3, 1L);
                stmt.setObject(4, addressId, java.sql.Types.BIGINT);
                stmt.setString(5, "Test Address, Building 1");
                stmt.setDouble(6, 100.0);
                stmt.setString(7, PaymentStatus.PENDING.name());
                stmt.setString(8, PaymentMethodForOrder.CARD.name());
                stmt.setTimestamp(9, Timestamp.from(Instant.now()));
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("id");
                }
            } catch (SQLException e) {
                // If status column doesn't exist, try without it
                if (e.getMessage().contains("column \"status\"")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO orders (delivery_address, payment_status, payment_method) " +
                             "VALUES (?, ?, ?) RETURNING id")) {
                        stmt.setString(1, "Test Address, Building 1");
                        stmt.setString(2, PaymentStatus.PENDING.name());
                        stmt.setString(3, PaymentMethodForOrder.CARD.name());
                        
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            return rs.getLong("id");
                        }
                    }
                }
                throw e;
            }
        }
        throw new SQLException("Failed to create order");
    }

    private static void deletePayment(Long paymentId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM payments WHERE id = ?")) {
            stmt.setLong(1, paymentId);
            stmt.executeUpdate();
        }
    }

    private static void deleteOrder(Long orderId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }

    private static void deleteClient(Long clientId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
            stmt.setLong(1, clientId);
            stmt.executeUpdate();
        }
    }

    private static void deleteAddress(Long addressId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM addresses WHERE id = ?")) {
            stmt.setLong(1, addressId);
            stmt.executeUpdate();
        }
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Save payment: Should save and return ID")
    void testSave_ShouldReturnId() throws SQLException {
        // Use existing order or create one
        Long orderId = getOrCreateTestOrder();
        
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(100.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();

        Long id = paymentRepository.save(payment);
        testPaymentId = id;

        assertNotNull(id);
        assertTrue(id > 0);
        
        // Verify payment was saved
        Optional<Payment> found = paymentRepository.findByOrderId(orderId);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
    }
    
    private static Long getOrCreateTestOrder() throws SQLException {
        // Always create a new order to avoid foreign key issues
        // Use unique client and address for each test
        if (testClientId == null || testAddressId == null) {
            testClientId = createDummyClient();
            testAddressId = createDummyAddress();
        }
        return createDummyOrder(testClientId, testAddressId);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Save payment: Should handle all payment methods")
    void testSave_AllPaymentMethods() throws SQLException {
        Long orderId = getOrCreateTestOrder();
        
        // Test CARD
        Payment cardPayment = Payment.builder()
                .orderId(orderId)
                .amount(50.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long cardId = paymentRepository.save(cardPayment);
        assertNotNull(cardId);
        deletePayment(cardId);
        
        // Test CASH
        Long orderId2 = getOrCreateTestOrder();
        Payment cashPayment = Payment.builder()
                .orderId(orderId2)
                .amount(75.0)
                .method(PaymentMethodForOrder.CASH)
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        Long cashId = paymentRepository.save(cashPayment);
        assertNotNull(cashId);
        deletePayment(cashId);
        deleteOrder(orderId2);
        
        // Test ONLINE
        Long orderId3 = getOrCreateTestOrder();
        Payment onlinePayment = Payment.builder()
                .orderId(orderId3)
                .amount(90.0)
                .method(PaymentMethodForOrder.ONLINE)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long onlineId = paymentRepository.save(onlinePayment);
        assertNotNull(onlineId);
        deletePayment(onlineId);
        deleteOrder(orderId3);
        
        deleteOrder(orderId);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Save payment: Should handle all payment statuses")
    void testSave_AllPaymentStatuses() throws SQLException {
        Long orderId = getOrCreateTestOrder();
        
        // Test PENDING
        Payment pendingPayment = Payment.builder()
                .orderId(orderId)
                .amount(60.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        Long pendingId = paymentRepository.save(pendingPayment);
        assertNotNull(pendingId);
        deletePayment(pendingId);
        
        // Test SUCCESS
        Long orderId2 = getOrCreateTestOrder();
        Payment successPayment = Payment.builder()
                .orderId(orderId2)
                .amount(70.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long successId = paymentRepository.save(successPayment);
        assertNotNull(successId);
        deletePayment(successId);
        deleteOrder(orderId2);
        
        // Test FAILED
        Long orderId3 = getOrCreateTestOrder();
        Payment failedPayment = Payment.builder()
                .orderId(orderId3)
                .amount(80.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.FAILED)
                .createdAt(Instant.now())
                .build();
        Long failedId = paymentRepository.save(failedPayment);
        assertNotNull(failedId);
        deletePayment(failedId);
        deleteOrder(orderId3);
        
        deleteOrder(orderId);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("FindByOrderId: Should return payment when exists")
    void testFindByOrderId_Exists() throws SQLException {
        Long orderId = getOrCreateTestOrder();
        
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(150.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        
        Long paymentId = paymentRepository.save(payment);
        
        Optional<Payment> found = paymentRepository.findByOrderId(orderId);
        
        assertTrue(found.isPresent());
        Payment foundPayment = found.get();
        assertEquals(paymentId, foundPayment.getId());
        assertEquals(orderId, foundPayment.getOrderId());
        assertEquals(150.0, foundPayment.getAmount(), 0.001);
        assertEquals(PaymentMethodForOrder.CARD, foundPayment.getMethod());
        assertEquals(PaymentStatus.SUCCESS, foundPayment.getStatus());
        assertNotNull(foundPayment.getCreatedAt());
        
        deletePayment(paymentId);
        deleteOrder(orderId);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("FindByOrderId: Should return empty when not found")
    void testFindByOrderId_NotFound() throws SQLException {
        Optional<Payment> found = paymentRepository.findByOrderId(-999L);
        assertFalse(found.isPresent());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("Save payment: Should map all fields correctly")
    void testSave_MapAllFields() throws SQLException {
        Long orderId = getOrCreateTestOrder();
        Instant createdAt = Instant.now();
        
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(250.75)
                .method(PaymentMethodForOrder.ONLINE)
                .status(PaymentStatus.SUCCESS)
                .createdAt(createdAt)
                .build();
        
        Long paymentId = paymentRepository.save(payment);
        
        Optional<Payment> found = paymentRepository.findByOrderId(orderId);
        assertTrue(found.isPresent());
        
        Payment foundPayment = found.get();
        assertEquals(paymentId, foundPayment.getId());
        assertEquals(orderId, foundPayment.getOrderId());
        assertEquals(250.75, foundPayment.getAmount(), 0.001);
        assertEquals(PaymentMethodForOrder.ONLINE, foundPayment.getMethod());
        assertEquals(PaymentStatus.SUCCESS, foundPayment.getStatus());
        assertNotNull(foundPayment.getCreatedAt());
        
        deletePayment(paymentId);
        deleteOrder(orderId);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("Save payment: Should handle different amounts")
    void testSave_DifferentAmounts() throws SQLException {
        Long orderId1 = getOrCreateTestOrder();
        Long orderId2 = getOrCreateTestOrder();
        Long orderId3 = getOrCreateTestOrder();
        
        // Small amount
        Payment small = Payment.builder()
                .orderId(orderId1)
                .amount(0.01)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long id1 = paymentRepository.save(small);
        deletePayment(id1);
        deleteOrder(orderId1);
        
        // Large amount
        Payment large = Payment.builder()
                .orderId(orderId2)
                .amount(999999.99)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long id2 = paymentRepository.save(large);
        deletePayment(id2);
        deleteOrder(orderId2);
        
        // Zero amount (edge case)
        Payment zero = Payment.builder()
                .orderId(orderId3)
                .amount(0.0)
                .method(PaymentMethodForOrder.CARD)
                .status(PaymentStatus.SUCCESS)
                .createdAt(Instant.now())
                .build();
        Long id3 = paymentRepository.save(zero);
        deletePayment(id3);
        deleteOrder(orderId3);
    }
}
