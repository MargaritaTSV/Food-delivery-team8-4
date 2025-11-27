package com.team8.fooddelivery.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты подключения к базе данных")
public class DatabaseConnectionTest {

  // Единые учетные данные (совместимость со скриптами)
  private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/food_delivery";
  private static final String DEFAULT_DB_USER = "postgres";
  private static final String DEFAULT_DB_PASSWORD = "postgres";

  @BeforeEach
  void setUp() {
    //DatabaseConnection.initializeDatabase();
    // Используем системные свойства или значения по умолчанию
    String dbUrl = System.getProperty("db.url", DEFAULT_DB_URL);
    String dbUser = System.getProperty("db.user", DEFAULT_DB_USER);
    String dbPassword = System.getProperty("db.password", DEFAULT_DB_PASSWORD);
    DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

    System.out.println("Тестовые параметры подключения:");
    System.out.println("URL: " + dbUrl);
    System.out.println("User: " + dbUser);
    System.out.println("Password: " + (dbPassword.isEmpty() ? "(empty)" : "***"));

    // Инициализируем структуру БД
    try {
      DatabaseConnection.initializeDatabase();
      System.out.println("✅ База данных успешно инициализирована");
    } catch (Exception e) {
      System.err.println("❌ Ошибка инициализации БД: " + e.getMessage());
      throw new RuntimeException("Не удалось инициализировать тестовую БД", e);
    }
  }

  @Test
  @DisplayName("Проверка тестового подключения к БД")
  void testConnection() {
    boolean connected = DatabaseConnection.testConnection();

    if (!connected) {
      System.err.println("\n❌ Не удалось подключиться к БД с параметрами:");
      System.err.println("URL: " + DEFAULT_DB_URL);
      System.err.println("User: " + DEFAULT_DB_USER);
      System.err.println("\nВыполните настройку: ./setup_database.sh");
    }

    assertTrue(connected, "Подключение к базе данных должно быть успешным. " +
        "Выполните ./setup_database.sh для настройки БД");
  }

  @Test
  @DisplayName("Получение подключения к БД")
  void testGetConnection() throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {
      assertNotNull(connection, "Подключение не должно быть null");
      assertFalse(connection.isClosed(), "Подключение должно быть открытым");

      // Дополнительная проверка валидности подключения
      assertTrue(connection.isValid(2), "Подключение должно быть валидным");
    }
  }

  @Test
  @DisplayName("Проверка закрытия подключения")
  void testCloseConnection() throws SQLException {
    Connection connection = DatabaseConnection.getConnection();
    assertNotNull(connection);
    assertFalse(connection.isClosed());

    // Закрываем подключение
    connection.close();
    assertTrue(connection.isClosed(), "Подключение должно быть закрыто");
  }

  @Test
  @DisplayName("Проверка установки параметров подключения")
  void testSetConnectionParams() {
    String testUrl = "jdbc:postgresql://localhost:5432/test_db";
    String testUser = "test_user";
    String testPassword = "test_password";

    // Проверяем, что метод не бросает исключений
    assertDoesNotThrow(() ->
        DatabaseConnection.setConnectionParams(testUrl, testUser, testPassword)
    );

    // Возвращаем оригинальные параметры для следующих тестов
    DatabaseConnection.setConnectionParams(DEFAULT_DB_URL, DEFAULT_DB_USER, DEFAULT_DB_PASSWORD);
  }

  @Test
  @DisplayName("Проверка работы с параметрами по умолчанию")
  void testDefaultParameters() throws SQLException {
    // Явно устанавливаем параметры по умолчанию
    DatabaseConnection.setConnectionParams(DEFAULT_DB_URL, DEFAULT_DB_USER, DEFAULT_DB_PASSWORD);

    try (Connection connection = DatabaseConnection.getConnection()) {
      assertNotNull(connection, "Должно быть возможно подключение с параметрами по умолчанию");
      assertTrue(connection.isValid(2), "Подключение должно быть валидным");
    }
  }

  @Test
  @DisplayName("Проверка поведения при неверных параметрах")
  void testInvalidParameters() {
    // Устанавливаем неверные параметры
    DatabaseConnection.setConnectionParams(
        "jdbc:postgresql://localhost:5432/nonexistent_db",
        "invalid_user",
        "wrong_password"
    );

    // Ожидаем, что тест подключения вернет false
    boolean connected = DatabaseConnection.testConnection();
    assertFalse(connected, "Подключение с неверными параметрами должно завершиться ошибкой");

    // Проверяем, что получение подключения бросает исключение
    assertThrows(SQLException.class, () -> {
      DatabaseConnection.getConnection();
    }, "При неверных параметрах должно бросаться SQLException");

    // Возвращаем валидные параметры
    DatabaseConnection.setConnectionParams(DEFAULT_DB_URL, DEFAULT_DB_USER, DEFAULT_DB_PASSWORD);
  }

  @Test
  @DisplayName("Проверка многократного открытия/закрытия подключения")
  void testMultipleConnections() throws SQLException {
    // Открываем несколько подключений и проверяем их независимость
    try (Connection conn1 = DatabaseConnection.getConnection();
        Connection conn2 = DatabaseConnection.getConnection()) {

      assertNotNull(conn1);
      assertNotNull(conn2);
      assertNotSame(conn1, conn2, "Должны создаваться разные экземпляры подключений");
      assertTrue(conn1.isValid(2));
      assertTrue(conn2.isValid(2));
    }
  }
}