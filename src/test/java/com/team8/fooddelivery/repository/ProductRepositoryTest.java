package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для ProductRepository, использующий реальное подключение к БД (без моков).
 * Тестирует CRUD и поисковые операции для продуктов.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductRepositoryTest { // Public class

  private ProductRepository productRepository;
  private AddressRepository addressRepository;
  private WorkingHoursRepository workingHoursRepository;
  private ShopRepository shopRepository;
  private static Long TEST_SHOP_ID; // ID созданного тестового магазина
  private static Long testProductId1;
  private static Long testProductId2;
  private static Long testAddressId;
  private static Long testWorkingHoursId;

  @BeforeAll
  public static void setupTestShop() throws SQLException {
    // Создаем тестовый магазин перед всеми тестами
    AddressRepository addrRepo = new AddressRepository();
    WorkingHoursRepository hoursRepo = new WorkingHoursRepository();
    ShopRepository shopRepo = new ShopRepository();

    // Создаем адрес
    Address address = Address.builder()
        .country("Russia")
        .city("Test City")
        .street("Test Street")
        .building("1")
        .build();
    testAddressId = addrRepo.save(address);

    // Создаем рабочие часы
    WorkingHours workingHours = new WorkingHours(
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "10:00-22:00",
        "10:00-22:00"
    );
    testWorkingHoursId = hoursRepo.save(workingHours);

    // Создаем магазин
    long suffix = System.currentTimeMillis();
    Shop shop = new Shop();
    shop.setNaming("Test Shop " + suffix);
    shop.setDescription("Test shop for ProductRepositoryTest");
    shop.setPublicEmail("shop" + suffix + "@test.com");
    shop.setEmailForAuth("shop_auth" + suffix + "@test.com");
    shop.setPhoneForAuth("+7999" + (suffix % 10000000));
    shop.setPublicPhone("+7999" + ((suffix + 1) % 10000000));
    shop.setStatus(ShopStatus.APPROVED);
    shop.setType(ShopType.RESTAURANT);
    shop.setOwnerName("Test Owner");
    shop.setOwnerContactPhone("+7999" + ((suffix + 2) % 10000000));
    shop.setPassword("test_password");
    shop.setAddress(address);
    shop.setWorkingHours(workingHours);

    TEST_SHOP_ID = shopRepo.save(shop);
  }

  @AfterAll
  public static void cleanUp() { // Public static method
    // Удаляем продукты
    if (TEST_SHOP_ID != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          var stmt = conn.prepareStatement("DELETE FROM products WHERE shop_id = ?")) {
        stmt.setLong(1, TEST_SHOP_ID);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при очистке продуктов: " + e.getMessage());
      }
    }

    // Удаляем магазин
    if (TEST_SHOP_ID != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement("DELETE FROM shops WHERE shop_id = ?")) {
        stmt.setLong(1, TEST_SHOP_ID);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при удалении магазина: " + e.getMessage());
      }
    }

    // Удаляем адрес
    if (testAddressId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement("DELETE FROM addresses WHERE id = ?")) {
        stmt.setLong(1, testAddressId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при удалении адреса: " + e.getMessage());
      }
    }

    // Удаляем рабочие часы
    if (testWorkingHoursId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement stmt = conn.prepareStatement("DELETE FROM working_hours WHERE id = ?")) {
        stmt.setLong(1, testWorkingHoursId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при удалении рабочих часов: " + e.getMessage());
      }
    }
  }

  @BeforeEach
  public void setUp() { // Public method
    productRepository = new ProductRepository();
  }

  // --- Тесты Save ---

  @Test
  @Order(1)
  @DisplayName("1. Сохранение продукта для магазина (saveForShop)")
  public void saveForShop_shouldInsertProductAndReturnId() throws SQLException { // Public method
    // 1. Arrange (Подготовка)
    Product burger = new Product(null, "Big Test Burger", "Классический тестовый бургер",
        250.0, 15.99, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));

    // 2. Act (Действие: Сохранение)
    Long id = productRepository.saveForShop(TEST_SHOP_ID, burger);
    testProductId1 = id;

    // 3. Assert (Проверка сохранения)
    assertNotNull(id, "Сохраненный ID продукта не должен быть null");

    // 4. Проверка через findById
    Optional<Product> foundProduct = productRepository.findById(id);
    assertTrue(foundProduct.isPresent(), "Продукт должен быть найден после сохранения");
    assertEquals("Big Test Burger", foundProduct.get().getName());
    assertEquals(Duration.ofMinutes(10), foundProduct.get().getCookingTimeMinutes());
  }

  @Test
  @Order(2)
  @DisplayName("2. Добавление второго продукта для категории поиска")
  public void saveSecondProduct_shouldBeFindable() throws SQLException { // Public method
    // 1. Arrange (Подготовка)
    Product fries = new Product(null, "Test Fries", "Тестовая картошка фри",
        150.0, 5.00, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(5));

    // 2. Act (Действие: Сохранение)
    Long id = productRepository.saveForShop(TEST_SHOP_ID, fries);
    testProductId2 = id;

    // 3. Assert (Проверка)
    assertNotNull(id);

    Optional<Product> foundProduct = productRepository.findById(id);
    assertTrue(foundProduct.isPresent());
    assertEquals(ProductCategory.MAIN_DISH, foundProduct.get().getCategory());
  }

  // --- Тесты Read (Поиск) ---

  @Test
  @Order(3)
  @DisplayName("3. Поиск по ID продукта")
  public void findById_shouldReturnProduct() throws SQLException { // Public method
    // 1. Arrange (Подготовка)
    assertNotNull(testProductId1, "ID продукта должен быть установлен");

    // 2. Act (Действие)
    Optional<Product> result = productRepository.findById(testProductId1);

    // 3. Assert (Проверка)
    assertTrue(result.isPresent(), "Продукт должен быть найден");
    assertEquals("Big Test Burger", result.get().getName());
  }

  @Test
  @Order(4)
  @DisplayName("4. Поиск всех продуктов магазина")
  public void findByShopId_shouldReturnAllProducts() throws SQLException { // Public method
    // 1. Act (Действие)
    List<Product> products = productRepository.findByShopId(TEST_SHOP_ID);

    // 2. Assert (Проверка)
    assertFalse(products.isEmpty(), "Список не должен быть пустым");
    assertEquals(2, products.size(), "Должно быть найдено 2 продукта");
    assertTrue(products.stream().anyMatch(p -> p.getName().equals("Big Test Burger")));
  }

  @Test
  @Order(5)
  @DisplayName("5. Поиск продуктов по ID магазина и Категории")
  public void findByShopIdAndCategory_shouldReturnFilteredProducts() throws SQLException { // Public method
    // 1. Act (Действие)
    List<Product> mains = productRepository.findByShopIdAndCategory(TEST_SHOP_ID, ProductCategory.MAIN_DISH);

    // 2. Assert (Проверка)
    // Оба продукта имеют категорию MAIN_DISH, поэтому должно быть 2
    assertEquals(2, mains.size(), "Должно быть найдено 2 продукта категории MAIN_DISH");
    assertTrue(mains.stream().anyMatch(p -> p.getName().equals("Big Test Burger")));
    assertTrue(mains.stream().anyMatch(p -> p.getName().equals("Test Fries")));
  }

  // --- Тест Update ---

  @Test
  @Order(6)
  @DisplayName("6. Обновление продукта")
  public void update_shouldModifyProductDetails() throws SQLException { // Public method
    // 1. Arrange (Получаем продукт для обновления)
    Product productToUpdate = productRepository.findById(testProductId1)
        .orElseThrow(() -> new AssertionError("Продукт не найден для обновления"));

    // Изменяем поля
    productToUpdate.setName("Updated Burger");
    productToUpdate.setAvailable(false);
    productToUpdate.setCookingTimeMinutes(Duration.ofMinutes(15)); // Новое время

    // 2. Act (Действие: Обновление)
    productRepository.update(productToUpdate);

    // 3. Assert (Проверка обновления)
    Optional<Product> updatedProductOpt = productRepository.findById(testProductId1);
    assertTrue(updatedProductOpt.isPresent());

    Product updatedProduct = updatedProductOpt.get();
    assertEquals("Updated Burger", updatedProduct.getName(), "Имя должно быть обновлено");
    assertFalse(updatedProduct.getAvailable(), "Доступность должна быть 'false'");
    assertEquals(Duration.ofMinutes(15), updatedProduct.getCookingTimeMinutes(), "Время готовки должно быть обновлено");
  }

  // --- Тест Delete ---

  @Test
  @Order(7)
  @DisplayName("7. Удаление продукта")
  public void delete_shouldRemoveProduct() throws SQLException { // Public method
    // 1. Arrange (Подготовка)
    assertNotNull(testProductId1, "ID продукта должен быть установлен");

    // 2. Act (Действие: Удаление)
    productRepository.delete(testProductId1);

    // 3. Assert (Проверка)
    Optional<Product> result = productRepository.findById(testProductId1);
    assertFalse(result.isPresent(), "Продукт не должен быть найден после удаления");

    // Удаляем второй продукт для завершения очистки (для идемпотентности)
    productRepository.delete(testProductId2);
    testProductId1 = null;
    testProductId2 = null;
  }
}