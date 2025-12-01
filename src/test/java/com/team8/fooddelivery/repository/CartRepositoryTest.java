package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartRepositoryTest {

  private final CartRepository repository = new CartRepository();
  private static Long testClientId;

  // --- Настройка окружения ---

  @BeforeAll
  static void init() throws SQLException {
    // Создаем клиента в БД, чтобы к нему можно было привязать корзину
    testClientId = createDummyClient();
  }

  @AfterAll
  static void tearDown() throws SQLException {
    // Удаляем тестового клиента (каскадно удалятся и корзины)
    deleteDummyClient(testClientId);
  }

  @BeforeEach
  void cleanCarts() throws SQLException {
    // Чистим корзины и их элементы этого клиента перед каждым тестом
    try (Connection conn = DatabaseConnection.getConnection()) {
      // Получаем все cart_id для этого клиента
      List<Long> cartIds = new ArrayList<>();
      try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM carts WHERE client_id = ?")) {
        stmt.setLong(1, testClientId);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            cartIds.add(rs.getLong("id"));
          }
        }
      }
      
      // Удаляем элементы корзины для каждого cart_id
      for (Long cartId : cartIds) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
          stmt.setLong(1, cartId);
          stmt.executeUpdate();
        }
      }
      
      // Затем удаляем корзины
      try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
        stmt.setLong(1, testClientId);
        stmt.executeUpdate();
      }
    }
  }

  // --- ТЕСТЫ ---

  @Test
  @Order(1)
  @DisplayName("Save & Find: Создание корзины и поиск по ID")
  void testSaveAndFindById() throws SQLException {
    Cart cart = new Cart();
    cart.setClientId(testClientId);

    // 1. Save
    Long cartId = repository.save(cart);
    assertNotNull(cartId, "ID новой корзины не должен быть null");

    // 2. Find By ID
    Optional<Cart> found = repository.findById(cartId);
    assertTrue(found.isPresent(), "Корзина должна находиться по ID");
    assertEquals(testClientId, found.get().getClientId());

    // 3. Find By Client ID
    Optional<Cart> foundByClient = repository.findByClientId(testClientId);
    assertTrue(foundByClient.isPresent());
    assertEquals(cartId, foundByClient.get().getId());
  }

  @Test
  @Order(2)
  @DisplayName("Items: Добавление, Обновление, Чтение списка")
  void testCartItemsLifecycle() throws SQLException {
    // Подготовка: создаем корзину
    Cart cart = new Cart();
    cart.setClientId(testClientId);
    Long cartId = repository.save(cart);

    // Создаем продукт для теста
    Long productId = createTestProduct();

    // 1. Save Item
    CartItem item = CartItem.builder()
        .cartId(cartId)
        .productId(productId)
        .productName("Pizza")
        .quantity(1)
        .price(500.0)
        .build();

    Long itemId = repository.saveCartItem(item);
    assertNotNull(itemId);

    // 2. Find Items (проверяем оба метода)
    List<CartItem> items1 = repository.findCartItemsByCartId(cartId);
    assertEquals(1, items1.size());
    assertEquals("Pizza", items1.get(0).getProductName());

    // Проверяем метод с передачей connection (он у тебя публичный)
    try (Connection conn = DatabaseConnection.getConnection()) {
      List<CartItem> items2 = repository.findCartItemsByCartId(cartId, conn);
      assertEquals(1, items2.size());
    }

    // 3. Update Item
    CartItem itemToUpdate = items1.get(0);
    itemToUpdate.setQuantity(5);
    itemToUpdate.setPrice(2500.0);

    repository.updateCartItem(itemToUpdate);

    // Проверяем обновление
    List<CartItem> updatedItems = repository.findCartItemsByCartId(cartId);
    assertEquals(5, updatedItems.get(0).getQuantity());
    assertEquals(2500.0, updatedItems.get(0).getPrice());
  }

  @Test
  @Order(3)
  @DisplayName("Delete: Удаление товара по ID")
  void testDeleteCartItem() throws SQLException {
    Long cartId = createCartWithItem();
    List<CartItem> items = repository.findCartItemsByCartId(cartId);
    Long itemId = items.get(0).getId();

    // Удаляем
    repository.deleteCartItem(itemId);

    // Проверяем
    assertTrue(repository.findCartItemsByCartId(cartId).isEmpty());
  }

  @Test
  @Order(4)
  @DisplayName("Delete: Удаление товара по ProductId")
  void testDeleteCartItemByProductId() throws SQLException {
    // Создаем корзину с товаром
    Long cartId = createCartWithItem();
    
    // Получаем productId из созданного товара
    List<CartItem> items = repository.findCartItemsByCartId(cartId);
    assertFalse(items.isEmpty());
    Long productId = items.get(0).getProductId();

    // Удаляем по productId
    repository.deleteCartItemByProductId(cartId, productId);

    // Проверяем
    assertTrue(repository.findCartItemsByCartId(cartId).isEmpty());
  }

  @Test
  @Order(5)
  @DisplayName("Clear: Полная очистка корзины")
  void testClearCart() throws SQLException {
    Cart cart = new Cart();
    cart.setClientId(testClientId);
    Long cartId = repository.save(cart);

    Long productId1 = createTestProduct();
    Long productId2 = createTestProduct();

    // Добавляем 2 товара
    repository.saveCartItem(CartItem.builder().cartId(cartId).productId(productId1).productName("A").quantity(1).price(10.0).build());
    repository.saveCartItem(CartItem.builder().cartId(cartId).productId(productId2).productName("B").quantity(1).price(10.0).build());

    // Очищаем
    repository.clearCart(cartId);

    // Проверяем
    assertTrue(repository.findCartItemsByCartId(cartId).isEmpty());
  }

  @Test
  @Order(6)
  @DisplayName("Delete Cart: Удаление самой корзины")
  void testDeleteCart() throws SQLException {
    Cart cart = new Cart();
    cart.setClientId(testClientId);
    Long cartId = repository.save(cart);

    repository.delete(cartId);

    Optional<Cart> found = repository.findById(cartId);
    assertFalse(found.isPresent(), "Корзина должна быть удалена");
  }

  @Test
  @Order(7)
  @DisplayName("Negative: Поиск несуществующей корзины")
  void testFindNotFound() throws SQLException {
    Optional<Cart> found = repository.findById(9999999L);
    assertFalse(found.isPresent());

    Optional<Cart> foundClient = repository.findByClientId(9999999L);
    assertFalse(foundClient.isPresent());
  }

  // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

  private Long createCartWithItem() throws SQLException {
    Cart cart = new Cart();
    cart.setClientId(testClientId);
    Long cartId = repository.save(cart);

    Long productId = createTestProduct();

    CartItem item = CartItem.builder()
        .cartId(cartId)
        .productId(productId)
        .productName("TestItem")
        .quantity(1)
        .price(100.0)
        .build();
    repository.saveCartItem(item);
    return cartId;
  }

  // Создаем клиента SQL-запросом, чтобы не зависеть от ClientRepository
  private static Long createDummyClient() throws SQLException {
    // Используем правильные поля из схемы БД
    String sql = "INSERT INTO clients (name, phone, email, password_hash, status, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      String uniquePhone = "+7999" + System.currentTimeMillis() % 10000000;
      stmt.setString(1, "RepoTestUser");
      stmt.setString(2, uniquePhone);
      stmt.setString(3, "repo_test_" + System.currentTimeMillis() + "@test.com");
      stmt.setString(4, "hashed_password");
      stmt.setString(5, "ACTIVE");
      stmt.setBoolean(6, true);
      stmt.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) return rs.getLong(1);
    }
    throw new SQLException("Не удалось создать тестового клиента");
  }

  private Long createTestProduct() throws SQLException {
    // Создаем тестовый продукт
    // Схема: name, description, weight, price, category, is_available, cooking_time_minutes, shop_id
    String sql = "INSERT INTO products (name, description, price, category, is_available, cooking_time_minutes, shop_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING product_id";
    
    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      
      // Создаем тестовый магазин, если его нет
      Long shopId = createTestShop();
      
      stmt.setString(1, "Test Product " + System.currentTimeMillis());
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

  private static void deleteDummyClient(Long id) throws SQLException {
    if (id == null) return;
    try (Connection conn = DatabaseConnection.getConnection()) {
      // Удаляем корзины
      try(PreparedStatement ps = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
        ps.setLong(1, id);
        ps.executeUpdate();
      }
      // Удаляем клиента
      try(PreparedStatement ps = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
        ps.setLong(1, id);
        ps.executeUpdate();
      }
    }
  }
}