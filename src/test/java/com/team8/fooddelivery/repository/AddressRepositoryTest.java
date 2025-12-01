package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.util.DatabaseConnection; // Assuming this provides a real connection
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AddressRepositoryTest {

  private AddressRepository addressRepository;
  private static Long testAddressId; // Для передачи ID между тестами

  @BeforeEach
  void setUp() {
    addressRepository = new AddressRepository();
  }

  // Вспомогательный метод для очистки тестовых данных, если ID был сохранен
  @AfterAll
  static void cleanUp() {
    if (testAddressId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          var stmt = conn.prepareStatement("DELETE FROM addresses WHERE id = ?")) {
        stmt.setLong(1, testAddressId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        // Логировать или обрабатывать ошибку очистки
        System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
      }
    }
  }

  /**
   * Тест: Создание (Save) адреса.
   * Проверяет, что адрес сохраняется и возвращается ID.
   */
  @Test
  @Order(1)
  void save_shouldInsertAddressAndReturnId() throws SQLException {
    // 1. Arrange (Подготовка)
    Address newAddress = Address.builder()
        .country("Testland")
        .city("TestCity")
        .street("TestStreet")
        .building("10A")
        .apartment("5")
        .entrance("1")
        .floor(2)
        .latitude(48.8584)
        .longitude(2.2945)
        .addressNote("Near the park")
        .district("Center")
        .build();

    // 2. Act (Действие)
    Long id = addressRepository.save(newAddress);
    testAddressId = id; // Сохраняем ID для последующих тестов и очистки

    // 3. Assert (Проверка)
    assertNotNull(id, "Сохраненный ID не должен быть null");
    assertTrue(id > 0, "ID должен быть положительным числом");

    // 4. Проверка через findById
    Optional<Address> foundAddress = addressRepository.findById(id);
    assertTrue(foundAddress.isPresent(), "Адрес должен быть найден после сохранения");

    Address savedAddress = foundAddress.get();
    assertEquals(newAddress.getCity(), savedAddress.getCity());
    assertEquals(newAddress.getFloor(), savedAddress.getFloor());
    assertEquals(newAddress.getLatitude(), savedAddress.getLatitude());
    // Добавьте больше проверок полей, если необходимо
  }

  /**
   * Тест: Чтение (Find) адреса по ID.
   * Зависит от успешного выполнения теста 'save'.
   */
  @Test
  @Order(2)
  void findById_shouldReturnAddress_whenExists() throws SQLException {
    // 1. Arrange (Подготовка)
    assertNotNull(testAddressId, "ID должен быть установлен в тесте save");

    // 2. Act (Действие)
    Optional<Address> result = addressRepository.findById(testAddressId);

    // 3. Assert (Проверка)
    assertTrue(result.isPresent(), "Адрес с testAddressId должен быть найден");
    assertEquals("TestCity", result.get().getCity());
  }

  /**
   * Тест: Обновление (Update) адреса.
   * Зависит от успешного выполнения теста 'save'.
   */
  @Test
  @Order(3)
  void update_shouldModifyExistingAddress() throws SQLException {
    // 1. Arrange (Подготовка)
    assertNotNull(testAddressId, "ID должен быть установлен в тесте save");

    Address updatedAddress = Address.builder()
        .country("UpdatedCountry")
        .city("UpdatedCity") // Измененное поле
        .street("TestStreet")
        .building("10A")
        .apartment("5")
        .entrance("1")
        .floor(3) // Измененное поле
        .latitude(48.8584)
        .longitude(2.2945)
        .addressNote("Updated Note") // Измененное поле
        .district("Center")
        .build();

    // 2. Act (Действие)
    addressRepository.update(testAddressId, updatedAddress);

    // 3. Assert (Проверка)
    Optional<Address> result = addressRepository.findById(testAddressId);
    assertTrue(result.isPresent(), "Адрес должен быть найден после обновления");

    Address actualAddress = result.get();
    assertEquals("UpdatedCity", actualAddress.getCity(), "Поле City должно быть обновлено");
    assertEquals(3, actualAddress.getFloor(), "Поле Floor должно быть обновлено");
    assertEquals("Updated Note", actualAddress.getAddressNote(), "Поле AddressNote должно быть обновлено");
    assertEquals("UpdatedCountry", actualAddress.getCountry(), "Поле Country должно быть обновлено");
  }

  /**
   * Тест: Удаление (Delete) адреса.
   * Зависит от успешного выполнения теста 'save'.
   */
  @Test
  @Order(4)
  void delete_shouldRemoveAddress() throws SQLException {
    // 1. Arrange (Подготовка)
    assertNotNull(testAddressId, "ID должен быть установлен в тесте save");

    // 2. Act (Действие)
    addressRepository.delete(testAddressId);

    // 3. Assert (Проверка)
    Optional<Address> result = addressRepository.findById(testAddressId);
    assertFalse(result.isPresent(), "Адрес не должен быть найден после удаления");

    // Сбрасываем ID, так как он больше не существует
    testAddressId = null;
  }

  /**
   * Тест: Поиск несуществующего адреса.
   */
  @Test
  void findById_shouldReturnEmptyOptional_whenNotFound() throws SQLException {
    // 1. Arrange (Подготовка)
    // Выбираем ID, который гарантированно не существует (например, очень большое отрицательное число)
    Long nonExistentId = -999L;

    // 2. Act (Действие)
    Optional<Address> result = addressRepository.findById(nonExistentId);

    // 3. Assert (Проверка)
    assertFalse(result.isPresent(), "Для несуществующего ID должен быть возвращен Optional.empty()");
  }
}