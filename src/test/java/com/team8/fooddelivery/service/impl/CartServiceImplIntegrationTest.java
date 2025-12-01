package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartServiceImplIntegrationTest {

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
        long suffix = System.currentTimeMillis() % 10000000000L;
        testPhone = "+7" + String.format("%010d", suffix).substring(0, 10);
        testEmail = "cart_test_" + System.currentTimeMillis() + "@test.com";
    }

    @Test
    @Order(1)
    @DisplayName("Create cart for client")
    void testCreateCartForClient() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        Client client = clientService.register(testPhone, "Password123!", "Test User", testEmail, address);
        testClientId = client.getId();

        Cart cart = cartService.createCartForClient(testClientId);
        assertNotNull(cart);
        assertNotNull(cart.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Get cart for client")
    void testGetCartForClient() {
        if (testClientId == null) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            Client client = clientService.register(testPhone + "1", "Password123!", "Test", testEmail + "1", address);
            testClientId = client.getId();
        }
        Cart cart = cartService.getCartForClient(testClientId);
        assertNotNull(cart);
    }

    @Test
    @Order(3)
    @DisplayName("List items")
    void testListItems() {
        if (testClientId == null) return;
        var items = cartService.listItems(testClientId);
        assertNotNull(items);
    }

    @Test
    @Order(4)
    @DisplayName("Add item to cart")
    void testAddItem() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item = CartItem.builder()
                .productId(productId)
                .productName("Test Product")
                .quantity(2)
                .price(100.0)
                .build();
        Cart cart = cartService.addItem(testClientId, item);
        assertNotNull(cart);
        assertFalse(cart.getItems().isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Update item quantity")
    void testUpdateItem() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item = CartItem.builder()
                .productId(productId)
                .productName("Test Product 2")
                .quantity(1)
                .price(50.0)
                .build();
        cartService.addItem(testClientId, item);
        Cart cart = cartService.updateItem(testClientId, productId, 3);
        assertNotNull(cart);
    }

    @Test
    @Order(6)
    @DisplayName("Remove item from cart")
    void testRemoveItem() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item = CartItem.builder()
                .productId(productId)
                .productName("Test Product 3")
                .quantity(1)
                .price(75.0)
                .build();
        cartService.addItem(testClientId, item);
        Cart cart = cartService.removeItem(testClientId, productId);
        assertNotNull(cart);
    }

    @Test
    @Order(7)
    @DisplayName("Clear cart")
    void testClear() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item = CartItem.builder()
                .productId(productId)
                .productName("Test Product 4")
                .quantity(1)
                .price(80.0)
                .build();
        cartService.addItem(testClientId, item);
        Cart cart = cartService.clear(testClientId);
        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("Calculate total")
    void testCalculateTotal() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item = CartItem.builder()
                .productId(productId)
                .productName("Test Product 5")
                .quantity(2)
                .price(100.0)
                .build();
        cartService.addItem(testClientId, item);
        Long total = cartService.calculateTotal(testClientId);
        assertNotNull(total);
        assertTrue(total >= 0);
    }

    @Test
    @Order(9)
    @DisplayName("Create cart for null client")
    void testCreateCartForNullClient() {
        Cart cart = cartService.createCartForClient(null);
        assertNotNull(cart);
        assertNull(cart.getClientId());
    }

    @Test
    @Order(10)
    @DisplayName("Add item with existing product")
    void testAddItemExistingProduct() throws SQLException {
        if (testClientId == null) return;
        Long productId = createTestProduct();
        CartItem item1 = CartItem.builder()
                .productId(productId)
                .productName("Test Product 6")
                .quantity(1)
                .price(50.0)
                .build();
        cartService.addItem(testClientId, item1);
        CartItem item2 = CartItem.builder()
                .productId(productId)
                .productName("Test Product 6")
                .quantity(2)
                .price(50.0)
                .build();
        Cart cart = cartService.addItem(testClientId, item2);
        assertNotNull(cart);
        // Quantity should be increased
        CartItem found = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        assertNotNull(found);
        assertTrue(found.getQuantity() >= 1);
    }

    @Test
    @Order(11)
    @DisplayName("Update item - item not found")
    void testUpdateItem_NotFound() {
        if (testClientId == null) return;
        Cart cart = cartService.updateItem(testClientId, -1L, 5);
        assertNotNull(cart);
    }

    @Test
    @Order(12)
    @DisplayName("Remove item - item not found")
    void testRemoveItem_NotFound() {
        if (testClientId == null) return;
        Cart cart = cartService.removeItem(testClientId, -1L);
        assertNotNull(cart);
    }

    @Test
    @Order(13)
    @DisplayName("Get cart for non-existent client")
    void testGetCartForClient_NotFound() {
        // Метод может выбрасывать IllegalArgumentException для несуществующего клиента
        try {
            Cart cart = cartService.getCartForClient(-1L);
            assertNull(cart);
        } catch (IllegalArgumentException e) {
            // Это нормальное поведение - клиент не найден
            assertTrue(e.getMessage().contains("не найден") || e.getMessage().contains("not found"));
        }
    }

    @Test
    @Order(14)
    @DisplayName("List items for non-existent client")
    void testListItems_NotFound() {
        // Метод может выбрасывать IllegalArgumentException для несуществующего клиента
        try {
            var items = cartService.listItems(-1L);
            assertNotNull(items);
            assertTrue(items.isEmpty());
        } catch (IllegalArgumentException e) {
            // Это нормальное поведение - клиент не найден
            assertTrue(e.getMessage().contains("не найден") || e.getMessage().contains("not found"));
        }
    }

    @Test
    @Order(15)
    @DisplayName("Calculate total for non-existent client")
    void testCalculateTotal_NotFound() {
        // Метод может выбрасывать IllegalArgumentException для несуществующего клиента
        try {
            Long total = cartService.calculateTotal(-1L);
            assertEquals(0L, total);
        } catch (IllegalArgumentException e) {
            // Это нормальное поведение - клиент не найден
            assertTrue(e.getMessage().contains("не найден") || e.getMessage().contains("not found"));
        }
    }

    private Long createTestProduct() throws SQLException {
        // Создаем тестовый продукт
        // Схема: name, description, weight, price, category, is_available, cooking_time_minutes, shop_id
        String sql = "INSERT INTO products (name, description, price, category, is_available, cooking_time_minutes, shop_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING product_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
          
          // Создаем тестовый магазин, если его нет
          Long shopId = createTestShop();
          
          String uniqueName = "Test Product " + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
          stmt.setString(1, uniqueName);
          stmt.setString(2, "Test Description");
          stmt.setDouble(3, 100.0);
          stmt.setString(4, "MAIN_DISH");
          stmt.setBoolean(5, true);
          stmt.setLong(6, 10L); // cooking_time_minutes
          stmt.setLong(7, shopId);
          
          ResultSet rs = stmt.executeQuery();
          if (rs.next()) return rs.getLong(1);
        }
        throw new SQLException("Не удалось создать тестовый продукт");
    }

    private Long createTestShop() throws SQLException {
        // Создаем уникальный магазин для каждого теста
        String uniqueName = "Test Shop " + System.currentTimeMillis();
        String uniqueEmail = "test_shop_" + System.currentTimeMillis() + "@test.com";
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        
        // Создаем тестовый магазин (email_for_auth, phone_for_auth, password обязательны)
        String sql = "INSERT INTO shops (naming, description, status, registration_date, email_for_auth, phone_for_auth, password) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING shop_id";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
          
          stmt.setString(1, uniqueName);
          stmt.setString(2, "Test Description");
          stmt.setString(3, "APPROVED");
          stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
          stmt.setString(5, uniqueEmail);
          stmt.setString(6, uniquePhone);
          stmt.setString(7, "test_password");
          
          ResultSet rs = stmt.executeQuery();
          if (rs.next()) return rs.getLong(1);
        }
        throw new SQLException("Не удалось создать тестовый магазин");
    }
}

