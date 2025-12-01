package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.Address; // Assuming Address model is imported
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для ClientRepository, использующий реальное подключение к БД (без моков).
 * Требует запущенной БД и функционального AddressRepository.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientRepositoryTest {

  private ClientRepository clientRepository;
  private static Long testClientId;
  // Используем уникальные данные для избежания конфликтов Unique Constraints (phone, email)
  private static final String TEST_PHONE = "+79991234567";
  private static final String TEST_EMAIL = "test_client_" + System.currentTimeMillis() + "@example.com";

  @BeforeEach
  void setUp() {
    clientRepository = new ClientRepository();
  }

  /**
   * Очистка данных после всех тестов.
   */
  @AfterAll
  static void cleanUp() {
    if (testClientId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          // Удаление клиента должно освободить address_id,
          // но сам адрес удаляется вручную или через триггер/CASCADE, если настроено.
          // Здесь мы просто удаляем клиента.
          var stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
        stmt.setLong(1, testClientId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
      }
    }
  }

  // --- Тесты CRUD для Client ---

  @Test
  @Order(1)
  @DisplayName("1. Сохранение клиента с адресом и историей заказов")
  void save_shouldInsertClientAndAddress() throws SQLException {
    // 1. Arrange (Подготовка)
    Address testAddress = Address.builder()
        .country("Russia").city("Moscow").street("Lenina").building("1A").apartment("10")
        .latitude(55.7558).longitude(37.6173).build();

    Client newClient = Client.builder()
        .name("Test Client")
        .phone(TEST_PHONE)
        .passwordHash("hashed_password_123")
        .email(TEST_EMAIL)
        .address(testAddress) // Вложенный объект
        .status(ClientStatus.ACTIVE)
        .createdAt(Instant.now())
        .isActive(true)
        .orderHistory(Arrays.asList("Order_1", "Order_2")) // Массив данных
        .build();

    // 2. Act (Действие: Сохранение)
    Long id = clientRepository.save(newClient);
    testClientId = id; // Сохраняем ID для последующих тестов

    // 3. Assert (Проверка сохранения)
    assertNotNull(id, "Сохраненный ID клиента не должен быть null");
    assertTrue(id > 0, "ID клиента должен быть положительным числом");

    // 4. Act (Действие: Поиск)
    Optional<Client> foundClient = clientRepository.findById(id);

    // 5. Assert (Проверка поиска и вложенных данных)
    assertTrue(foundClient.isPresent(), "Клиент должен быть найден после сохранения");

    Client savedClient = foundClient.get();
    assertEquals(TEST_PHONE, savedClient.getPhone());
    assertEquals(TEST_EMAIL, savedClient.getEmail());
    assertEquals(ClientStatus.ACTIVE, savedClient.getStatus());

    // Проверка вложенного адреса
    assertNotNull(savedClient.getAddress(), "Адрес должен быть загружен");
    assertEquals("Moscow", savedClient.getAddress().getCity());

    // Проверка массива OrderHistory
    assertFalse(savedClient.getOrderHistory().isEmpty(), "История заказов не должна быть пустой");
    assertEquals(2, savedClient.getOrderHistory().size());
    assertTrue(savedClient.getOrderHistory().contains("Order_1"));
  }

  @Test
  @Order(2)
  @DisplayName("2. Поиск по Phone и Email")
  void findByPhone_and_findByEmail_shouldReturnClient() throws SQLException {
    // 1. Act (Действие)
    Optional<Client> byPhone = clientRepository.findByPhone(TEST_PHONE);
    Optional<Client> byEmail = clientRepository.findByEmail(TEST_EMAIL);

    // 2. Assert (Проверка)
    assertTrue(byPhone.isPresent(), "Клиент должен быть найден по телефону");
    assertTrue(byEmail.isPresent(), "Клиент должен быть найден по email");
    assertEquals(testClientId, byPhone.get().getId());
    assertEquals(testClientId, byEmail.get().getId());
  }

  @Test
  @Order(3)
  @DisplayName("3. Обновление клиента, адреса и статуса")
  void update_shouldModifyClientAndAddress() throws SQLException {
    assertNotNull(testClientId, "ID клиента должен быть установлен");

    // 1. Arrange (Получаем текущий объект для обновления)
    Client clientToUpdate = clientRepository.findById(testClientId)
        .orElseThrow(() -> new AssertionError("Клиент не найден для обновления"));

    // Изменяем поля клиента
    clientToUpdate.setName("Updated Test Name");
    clientToUpdate.setStatus(ClientStatus.INACTIVE);
    clientToUpdate.setOrderHistory(Arrays.asList("Order_3", "Order_4", "Order_5"));

    // Изменяем поля вложенного адреса (обновление в AddressRepository)
    clientToUpdate.getAddress().setCity("St. Petersburg");

    // 2. Act (Действие: Обновление)
    clientRepository.update(clientToUpdate);

    // 3. Assert (Проверка обновления)
    Optional<Client> updatedClientOpt = clientRepository.findById(testClientId);
    assertTrue(updatedClientOpt.isPresent());

    Client updatedClient = updatedClientOpt.get();

    // Проверка полей клиента
    assertEquals("Updated Test Name", updatedClient.getName(), "Имя должно быть обновлено");
    assertEquals(ClientStatus.INACTIVE, updatedClient.getStatus(), "Статус должен быть обновлен");
    assertEquals(3, updatedClient.getOrderHistory().size(), "История заказов должна быть обновлена");

    // Проверка, что адрес также обновился
    assertNotNull(updatedClient.getAddress(), "Адрес должен остаться и быть загружен");
    assertEquals("St. Petersburg", updatedClient.getAddress().getCity(), "Город в адресе должен быть обновлен");
  }

  @Test
  @Order(4)
  @DisplayName("4. Удаление клиента")
  void delete_shouldRemoveClient() throws SQLException {
    assertNotNull(testClientId, "ID клиента должен быть установлен");

    // 1. Act (Действие)
    clientRepository.delete(testClientId);

    // 2. Assert (Проверка)
    Optional<Client> result = clientRepository.findById(testClientId);
    assertFalse(result.isPresent(), "Клиент не должен быть найден после удаления");

    // Сбрасываем ID, так как он больше не существует
    testClientId = null;
  }

  @Test
  @DisplayName("5. Поиск несуществующего клиента")
  void findById_shouldReturnEmptyOptional_whenNotFound() throws SQLException {
    // 1. Act (Действие)
    Optional<Client> result = clientRepository.findById(-1L);

    // 2. Assert (Проверка)
    assertFalse(result.isPresent(), "Для несуществующего ID должен быть возвращен Optional.empty()");
  }
}