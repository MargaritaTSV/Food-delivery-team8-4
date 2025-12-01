package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopRepositoryTest {

  private ShopRepository shopRepository;
  private static Long fullShopId;
  private static Long nullsShopId;

  // Данные для авторизации
  private static final String EMAIL_FULL = "full_" + System.currentTimeMillis() + "@test.com";
  private static final String PHONE_FULL = "+7900" + (System.currentTimeMillis() % 100000);
  private static final String EMAIL_NULL = "null_" + System.currentTimeMillis() + "@test.com";

  @BeforeEach
  public void setUp() {
    shopRepository = new ShopRepository();
  }

  @AfterAll
  public static void cleanUp() {
    // Очистка всех данных, созданных тестами
    deleteShopAndChildren(fullShopId);
    deleteShopAndChildren(nullsShopId);
  }

  // Вспомогательный метод для удаления (т.к. мы не знаем ID адресов в Java-модели)
  private static void deleteShopAndChildren(Long shopId) {
    if (shopId == null) return;
    try (Connection conn = DatabaseConnection.getConnection()) {
      // Сначала получаем ID вложенных сущностей
      Long addrId = null;
      Long hoursId = null;
      var stmt = conn.prepareStatement("SELECT address_id, working_hours_id FROM shops WHERE shop_id = ?");
      stmt.setLong(1, shopId);
      var rs = stmt.executeQuery();
      if (rs.next()) {
        addrId = rs.getObject("address_id", Long.class);
        hoursId = rs.getObject("working_hours_id", Long.class);
      }
      rs.close();
      stmt.close();

      // Удаляем магазин
      conn.createStatement().executeUpdate("DELETE FROM shops WHERE shop_id = " + shopId);

      // Удаляем сироты
      if (addrId != null) conn.createStatement().executeUpdate("DELETE FROM addresses WHERE id = " + addrId);
      if (hoursId != null) conn.createStatement().executeUpdate("DELETE FROM working_hours WHERE id = " + hoursId);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Test
  @Order(1)
  @DisplayName("1. Save: Полный магазин (покрытие веток if != null)")
  public void save_shouldSaveShopWithAllNestedEntities() throws SQLException {
    Address address = Address.builder()
        .country("Russia").city("Moscow").street("Arbat").building("1")
        .district("Center").latitude(55.0).longitude(37.0).build();

    WorkingHours hours = new WorkingHours("09-20", "09-20", "09-20", "09-20", "09-20", "10-18", "Closed");

    Shop shop = new Shop();
    shop.setNaming("Full Shop");
    shop.setDescription("Desc");
    shop.setPublicEmail("public@full.com");
    shop.setEmailForAuth(EMAIL_FULL);
    shop.setPhoneForAuth(PHONE_FULL);
    shop.setPublicPhone("88005553535");
    shop.setStatus(ShopStatus.APPROVED);
    shop.setType(ShopType.RESTAURANT);
    shop.setOwnerName("Owner");
    shop.setOwnerContactPhone("123");
    shop.setPassword("pass");
    shop.setAddress(address);
    shop.setWorkingHours(hours);
    shop.setProducts(new ArrayList<>());

    fullShopId = shopRepository.save(shop);

    assertNotNull(fullShopId);

    Optional<Shop> result = shopRepository.findById(fullShopId);
    assertTrue(result.isPresent());
    assertNotNull(result.get().getAddress(), "Адрес должен быть сохранен");
    assertEquals("Center", result.get().getAddress().getDistrict());
    assertNotNull(result.get().getWorkingHours(), "Часы должны быть сохранены");
    assertEquals("Closed", result.get().getWorkingHours().getSunday());
  }

  @Test
  @Order(2)
  @DisplayName("2. Save: Магазин без вложенных сущностей (покрытие веток if == null)")
  public void save_shouldSaveShopWithNullNestedEntities() throws SQLException {
    Shop shop = new Shop();
    shop.setNaming("Nulls Shop");
    shop.setEmailForAuth(EMAIL_NULL);
    shop.setPhoneForAuth("12345"); // Уникальный телефон
    shop.setStatus(ShopStatus.PENDING);
    shop.setPassword("pass");
    // Address и WorkingHours оставляем null

    nullsShopId = shopRepository.save(shop);

    assertNotNull(nullsShopId);

    Optional<Shop> result = shopRepository.findById(nullsShopId);
    assertTrue(result.isPresent());
    assertNull(result.get().getAddress());
    assertNull(result.get().getWorkingHours());
  }

  @Test
  @Order(3)
  @DisplayName("3. Find: Поиск по Auth полям (Email/Phone)")
  public void findByAuth_shouldReturnCorrectShop() throws SQLException {
    assertTrue(shopRepository.findByEmailForAuth(EMAIL_FULL).isPresent());
    assertTrue(shopRepository.findByPhoneForAuth(PHONE_FULL).isPresent());

    // Негативные тесты
    assertFalse(shopRepository.findByEmailForAuth("fake").isPresent());
    assertFalse(shopRepository.findByPhoneForAuth("fake").isPresent());
  }

  @Test
  @Order(4)
  @DisplayName("4. Update: Обновление полей и вложенных сущностей")
  public void update_shouldUpdateFields() throws SQLException {
    Shop shop = shopRepository.findById(fullShopId).get();
    shop.setNaming("Updated Name");
    shop.getAddress().setCity("Spb"); // Меняем поле в адресе
    shop.getWorkingHours().setMonday("Closed"); // Меняем поле в часах

    shopRepository.update(shop);

    Shop updated = shopRepository.findById(fullShopId).get();
    assertEquals("Updated Name", updated.getNaming());
    assertEquals("Spb", updated.getAddress().getCity());
    assertEquals("Closed", updated.getWorkingHours().getMonday());
  }

  @Test
  @Order(5)
  @DisplayName("5. Update: Добавление адреса магазину, у которого его не было")
  public void update_shouldAddNestedToNullShop() throws SQLException {
    Shop shop = shopRepository.findById(nullsShopId).get();
    assertNull(shop.getAddress());

    // Добавляем адрес
    shop.setAddress(Address.builder().city("NewCity").build());

    shopRepository.update(shop);

    Shop updated = shopRepository.findById(nullsShopId).get();
    assertNotNull(updated.getAddress());
    assertEquals("NewCity", updated.getAddress().getCity());
  }

  @Test
  @Order(6)
  @DisplayName("6. FindAll: Должен вернуть список")
  public void findAll_shouldReturnList() throws SQLException {
    List<Shop> all = shopRepository.findAll();
    assertTrue(all.size() >= 2);
  }

  @Test
  @Order(7)
  @DisplayName("7. Delete: Удаление магазина")
  public void delete_shouldRemoveShop() throws SQLException {
    shopRepository.delete(fullShopId);
    assertFalse(shopRepository.findById(fullShopId).isPresent());
    fullShopId = null; // Помечаем как удаленный для cleanUp
  }

  @Test
  @Order(8)
  @DisplayName("8. FindById: Несуществующий ID")
  public void findById_shouldReturnEmpty() throws SQLException {
    assertFalse(shopRepository.findById(-100L).isPresent());
  }
}