package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для CourierRepository, использующий реальное подключение к БД (без моков).
 * Требует запущенной БД.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourierRepositoryTest {

  private CourierRepository courierRepository;
  private static Long testCourierId;
  // Уникальный номер телефона для предотвращения конфликтов
  private static final String TEST_PHONE = "+79876543210";

  @BeforeEach
  void setUp() {
    courierRepository = new CourierRepository();
  }

  /**
   * Очистка тестовых данных после всех тестов.
   */
  @AfterAll
  static void cleanUp() {
    if (testCourierId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          var stmt = conn.prepareStatement("DELETE FROM couriers WHERE id = ?")) {
        stmt.setLong(1, testCourierId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
      }
    }
  }

  // --- Тесты CRUD ---

  @Test
  @Order(1)
  @DisplayName("1. Сохранение курьера")
  void save_shouldInsertCourierAndReturnId() throws SQLException {
    // 1. Arrange (Подготовка)
    Courier newCourier = new Courier();
    newCourier.setName("Test Courier Name");
    newCourier.setPassword("strong_hash");
    newCourier.setPhoneNumber(TEST_PHONE);
    newCourier.setStatus(CourierStatus.ON_SHIFT);
    newCourier.setTransportType("BICYCLE");
    newCourier.setCurrentBalance(100.50);
    newCourier.setBankCard(1234567890123456L);
    // currentOrderId остается null

    // 2. Act (Действие: Сохранение)
    Long id = courierRepository.save(newCourier);
    testCourierId = id; // Сохраняем ID для последующих тестов

    // 3. Assert (Проверка сохранения)
    assertNotNull(id, "Сохраненный ID курьера не должен быть null");
    assertTrue(id > 0, "ID курьера должен быть положительным числом");

    // 4. Act (Действие: Поиск)
    Optional<Courier> foundCourier = courierRepository.findById(id);

    // 5. Assert (Проверка поиска и полей)
    assertTrue(foundCourier.isPresent(), "Курьер должен быть найден после сохранения");

    Courier savedCourier = foundCourier.get();
    assertEquals(newCourier.getName(), savedCourier.getName());
    assertEquals(newCourier.getPhoneNumber(), savedCourier.getPhoneNumber());
    assertEquals(CourierStatus.ON_SHIFT, savedCourier.getStatus());
    assertEquals(100.50, savedCourier.getCurrentBalance(), 0.001);
    assertNull(savedCourier.getCurrentOrderId(), "current_order_id должен быть null");
  }

  @Test
  @Order(2)
  @DisplayName("2. Поиск курьера по ID")
  void findById_shouldReturnCourier_whenExists() throws SQLException {
    // 1. Arrange (Подготовка)
    assertNotNull(testCourierId, "ID курьера должен быть установлен в тесте save");

    // 2. Act (Действие)
    Optional<Courier> result = courierRepository.findById(testCourierId);

    // 3. Assert (Проверка)
    assertTrue(result.isPresent(), "Курьер с testCourierId должен быть найден");
    assertEquals(TEST_PHONE, result.get().getPhoneNumber());
  }

  @Test
  @Order(3)
  @DisplayName("3. Поиск курьера по номеру телефона")
  void findByPhoneNumber_shouldReturnCourier_whenExists() throws SQLException {
    // 1. Arrange (Подготовка)
    // TEST_PHONE установлен в тесте save

    // 2. Act (Действие)
    Optional<Courier> result = courierRepository.findByPhoneNumber(TEST_PHONE);

    // 3. Assert (Проверка)
    assertTrue(result.isPresent(), "Курьер должен быть найден по номеру телефона");
    assertEquals(testCourierId, result.get().getId());
  }

  @Test
  @Order(4)
  @DisplayName("4. Обновление курьера")
  void update_shouldModifyExistingCourier() throws SQLException {
    assertNotNull(testCourierId, "ID курьера должен быть установлен");

    // 1. Arrange (Получаем объект для обновления)
    Courier courierToUpdate = courierRepository.findById(testCourierId)
        .orElseThrow(() -> new AssertionError("Курьер не найден для обновления"));

    // Изменяем поля
    courierToUpdate.setStatus(CourierStatus.DELIVERING); // Меняем статус
    courierToUpdate.setCurrentOrderId(500L); // Назначаем заказ
    courierToUpdate.setCurrentBalance(150.00); // Меняем баланс
    courierToUpdate.setTransportType("CAR"); // Меняем тип транспорта

    // 2. Act (Действие: Обновление)
    courierRepository.update(courierToUpdate);

    // 3. Assert (Проверка обновления)
    Optional<Courier> updatedCourierOpt = courierRepository.findById(testCourierId);
    assertTrue(updatedCourierOpt.isPresent());

    Courier updatedCourier = updatedCourierOpt.get();
    assertEquals(CourierStatus.DELIVERING, updatedCourier.getStatus(), "Статус должен быть обновлен");
    assertEquals(500L, updatedCourier.getCurrentOrderId(), "ID заказа должен быть обновлен");
    assertEquals(150.00, updatedCourier.getCurrentBalance(), 0.001, "Баланс должен быть обновлен");
    assertEquals("CAR", updatedCourier.getTransportType(), "Тип транспорта должен быть обновлен");
  }

  @Test
  @Order(5)
  @DisplayName("5. Удаление курьера")
  void delete_shouldRemoveCourier() throws SQLException {
    assertNotNull(testCourierId, "ID курьера должен быть установлен");

    // 1. Act (Действие: Удаление)
    courierRepository.delete(testCourierId);

    // 2. Assert (Проверка)
    Optional<Courier> result = courierRepository.findById(testCourierId);
    assertFalse(result.isPresent(), "Курьер не должен быть найден после удаления");

    // Сбрасываем ID, так как он больше не существует
    testCourierId = null;
  }

  // --- Дополнительные тесты ---

  @Test
  @DisplayName("6. Поиск несуществующего курьера")
  void findById_shouldReturnEmptyOptional_whenNotFound() throws SQLException {
    // 1. Act (Действие)
    Optional<Courier> result = courierRepository.findById(-1L);

    // 2. Assert (Проверка)
    assertFalse(result.isPresent(), "Для несуществующего ID должен быть возвращен Optional.empty()");
  }

  @Test
  @DisplayName("7. Поиск всех курьеров")
  void findAll_shouldReturnListOfCouriers() throws SQLException {
    // Arrange: Сохраняем тестового курьера (если не был создан, или создаем нового)
    if (testCourierId == null) {
      Courier tempCourier = new Courier();
      tempCourier.setPhoneNumber("+71112223344");
      tempCourier.setName("Temp Courier");
      tempCourier.setPassword("temp");
      tempCourier.setStatus(CourierStatus.OFF_SHIFT);
      tempCourier.setTransportType("FOOT");
      Long tempId = courierRepository.save(tempCourier);

      // 1. Act (Действие)
      List<Courier> couriers = courierRepository.findAll();

      // 2. Assert (Проверка)
      assertNotNull(couriers);
      assertTrue(couriers.size() >= 1, "Список курьеров должен содержать хотя бы одного курьера");

      // Clean up the temporary courier
      courierRepository.delete(tempId);
    } else {
      // 1. Act (Действие)
      List<Courier> couriers = courierRepository.findAll();

      // 2. Assert (Проверка)
      assertNotNull(couriers);
      // Если testCourierId не null, то мы находимся между тестами 1 и 5,
      // и в списке должен быть как минимум один наш тестовый курьер.
      assertTrue(couriers.size() >= 1, "Список курьеров должен содержать хотя бы одного курьера");
    }
  }
}