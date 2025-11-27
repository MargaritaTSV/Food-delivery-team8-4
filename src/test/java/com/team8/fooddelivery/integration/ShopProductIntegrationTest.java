package com.team8.fooddelivery.integration;

import com.team8.fooddelivery.model.*;
import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.repository.*;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты магазинов и продуктов")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShopProductIntegrationTest {

  private AddressRepository addressRepository;
  private WorkingHoursRepository workingHoursRepository;
  private ShopRepository shopRepository;
  private ProductRepository productRepository;

  @BeforeAll
  static void setupDatabaseConnection() {
    DatabaseConnection.initializeDatabase();
    String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
    String dbUser = System.getProperty("db.user", "postgres");
    String dbPassword = System.getProperty("db.password", "postgres");
    DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

    if (!DatabaseConnection.testConnection()) {
      throw new RuntimeException("Не удалось подключиться к базе данных");
    }
  }

  @BeforeEach
  void setUp() {
    addressRepository = new AddressRepository();
    workingHoursRepository = new WorkingHoursRepository();
    shopRepository = new ShopRepository();
    productRepository = new ProductRepository();
  }

  @Test
  @DisplayName("Создание магазина с продуктами")
  void testShopWithProducts() throws SQLException {
    // 1. Создание адреса для магазина
    Address shopAddress = Address.builder()
        .country("Россия")
        .city("Москва")
        .street("Тверская")
        .building("5")
        .latitude(55.7558)
        .longitude(37.6173)
        .build();
    Long shopAddressId = addressRepository.save(shopAddress);

    // 2. Создание рабочих часов
    WorkingHours workingHours = new WorkingHours(
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "09:00-21:00",
        "10:00-22:00",
        "10:00-22:00"
    );
    Long workingHoursId = workingHoursRepository.save(workingHours);

    // 3. Создание магазина
    long suffix = System.currentTimeMillis();
    Shop shop = new Shop();
    shop.setNaming("Тестовый Ресторан " + suffix);
    shop.setDescription("Описание тестового ресторана");
    shop.setPublicEmail("restaurant" + suffix + "@test.com");
    shop.setEmailForAuth("restaurant_auth" + suffix + "@test.com");
    shop.setPhoneForAuth("+7999" + (suffix % 1_000_0000));
    shop.setPublicPhone("+7999" + ((suffix + 10) % 1_000_0000));
    shop.setStatus(ShopStatus.PENDING);
    shop.setAddress(shopAddress);
    shop.setWorkingHours(workingHours);
    shop.setOwnerName("Владелец Тест " + suffix);
    shop.setOwnerContactPhone("+7999" + ((suffix + 20) % 1_000_0000));
    shop.setRating(4.5);
    shop.setType(ShopType.RESTAURANT);
    shop.setPassword("shop_password");

    Long shopId = shopRepository.save(shop);
    assertNotNull(shopId);

    // 4. Создание продуктов
    Product product1 = new Product(
        null,
        "Пицца Пепперони",
        "Острая пицца с пепперони",
        500.0,
        600.0,
        ProductCategory.MAIN_DISH,
        true,
        Duration.ofMinutes(20)
    );
    Long product1Id = productRepository.saveForShop(shopId, product1);
    assertNotNull(product1Id);

    Product product2 = new Product(
        null,
        "Салат Цезарь",
        "Классический салат Цезарь",
        300.0,
        350.0,
        ProductCategory.MAIN_DISH,
        true,
        Duration.ofMinutes(10)
    );
    Long product2Id = productRepository.saveForShop(shopId, product2);
    assertNotNull(product2Id);

    // 5. Проверка продуктов
    List<Product> products = productRepository.findByShopId(shopId);
    assertEquals(2, products.size());
  }

  @Test
  @DisplayName("Создание магазина разных типов")
  void testDifferentShopTypes() throws SQLException {
    Long shopId1 = null;
    Long shopId2 = null;

    long suffix = System.currentTimeMillis();

    // Магазин типа RESTAURANT
    Shop restaurant = new Shop();
    restaurant.setNaming("Ресторан Премиум " + suffix);
    restaurant.setEmailForAuth("restaurant_premium" + suffix + "@test.com");
    restaurant.setPhoneForAuth("+7999" + (suffix % 1_000_0000));
    restaurant.setStatus(ShopStatus.APPROVED);
    restaurant.setType(ShopType.RESTAURANT);
    restaurant.setPassword("pass1");
    shopId1 = shopRepository.save(restaurant);

    // Магазин типа FAST_FOOD
    Shop fastFood = new Shop();
    fastFood.setNaming("ФастФуд Экспресс " + (suffix + 1));
    fastFood.setEmailForAuth("fastfood" + suffix + "@test.com");
    fastFood.setPhoneForAuth("+7999" + ((suffix + 1) % 1_000_0000));
    fastFood.setStatus(ShopStatus.APPROVED);
    fastFood.setType(ShopType.FAST_FOOD);
    fastFood.setPassword("pass2");
    shopId2 = shopRepository.save(fastFood);

    // Проверка сохранения
    assertNotNull(shopId1);
    assertNotNull(shopId2);
    assertNotEquals(shopId1, shopId2);

    Optional<Shop> retrievedRestaurant = shopRepository.findById(shopId1);
    Optional<Shop> retrievedFastFood = shopRepository.findById(shopId2);

    assertTrue(retrievedRestaurant.isPresent());
    assertTrue(retrievedFastFood.isPresent());
    assertEquals(ShopType.RESTAURANT, retrievedRestaurant.get().getType());
    assertEquals(ShopType.FAST_FOOD, retrievedFastFood.get().getType());
  }
}