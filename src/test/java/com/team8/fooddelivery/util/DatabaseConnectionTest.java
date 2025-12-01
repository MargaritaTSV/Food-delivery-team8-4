package com.team8.fooddelivery.util;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseConnectionTest {

  private static String originalUrl;
  private static String originalUser;
  private static String originalPass;

  // Сохраняем настройки перед тестами, чтобы не сломать остальные тесты
  @BeforeAll
  static void saveConfig() throws Exception {
    // Достаем текущие значения через рефлексию (так как геттеров нет)
    originalUrl = getStaticField("dbUrl");
    originalUser = getStaticField("dbUser");
    originalPass = getStaticField("dbPassword");
  }

  // Восстанавливаем настройки после каждого теста
  @AfterEach
  void restoreConfig() {
    DatabaseConnection.setConnectionParams(originalUrl, originalUser, originalPass);
  }

  // ---------------------------------------------------
  // 1. HAPPY PATH (Успешные сценарии)
  // ---------------------------------------------------

  @Test
  @Order(1)
  @DisplayName("1. getConnection: Успешное подключение к реальной БД")
  void testGetConnection_Success() throws SQLException {
    // Этот тест пройдет, только если у тебя поднята БД (как в прошлых тестах)
    try (Connection conn = DatabaseConnection.getConnection()) {
      assertNotNull(conn);
      assertFalse(conn.isClosed());
    }
  }

  @Test
  @Order(2)
  @DisplayName("2. testConnection: Возвращает true при рабочей БД")
  void testTestConnection_Success() {
    assertTrue(DatabaseConnection.testConnection(), "Должно быть true, если БД доступна");
  }

  @Test
  @Order(3)
  @DisplayName("3. initializeDatabase: Проверка вызова (идемпотентность)")
  void testInitializeDatabase() {
    // Просто проверяем, что метод не падает
    assertDoesNotThrow(DatabaseConnection::initializeDatabase);
  }

  @Test
  @Order(4)
  @DisplayName("4. closeConnection: Корректная работа с null")
  void testCloseConnection_Null() {
    // Не должно падать
    assertDoesNotThrow(() -> DatabaseConnection.closeConnection(null));
  }

  @Test
  @Order(5)
  @DisplayName("5. closeConnection: Закрытие реального соединения")
  void testCloseConnection_Real() throws SQLException {
    Connection conn = DatabaseConnection.getConnection();
    DatabaseConnection.closeConnection(conn);
    assertTrue(conn.isClosed());
  }

  // ---------------------------------------------------
  // 2. ERROR PATH (Ломаем настройки)
  // ---------------------------------------------------

  @Test
  @Order(6)
  @DisplayName("6. Ошибка подключения: Неверный URL (покрытие catch блока)")
  void testGetConnection_Failure() {
    // Устанавливаем "битый" URL
    DatabaseConnection.setConnectionParams("jdbc:postgresql://invalid-host:5432/db", "user", "pass");

    // 1. Проверяем getConnection (должен выбросить SQLException и залогировать ошибку)
    assertThrows(SQLException.class, DatabaseConnection::getConnection);

    // 2. Проверяем testConnection (должен вернуть false и залогировать ошибку)
    assertFalse(DatabaseConnection.testConnection());
  }

  // ---------------------------------------------------
  // 3. PRIVATE LOGIC (Метод resolve через Reflection)
  // Это даст покрытие логики выбора URL
  // ---------------------------------------------------

  @Test
  @Order(7)
  @DisplayName("7. Private resolve: Приоритет System Property")
  void testResolve_SystemProperty() throws Exception {
    String key = "test.db.prop";
    String envKey = "TEST_DB_ENV";

    // Устанавливаем системное свойство
    System.setProperty(key, "VALUE_FROM_SYSTEM");

    // Вызываем приватный метод resolve
    String result = invokeResolve(key, envKey, "default_local", "default_container");

    assertEquals("VALUE_FROM_SYSTEM", result);

    // Чистим
    System.clearProperty(key);
  }

  @Test
  @Order(8)
  @DisplayName("8. Private resolve: Fallback к дефолтному значению")
  void testResolve_Default() throws Exception {
    String key = "test.db.prop.missing";
    String envKey = "TEST_DB_ENV_MISSING";

    // Системного свойства нет, переменной окружения нет -> берем default
    String result = invokeResolve(key, envKey, "default_local", "default_container");

    assertEquals("default_local", result);
  }

  @Test
  @Order(9)
  @DisplayName("9. Private resolve: Fallback к контейнеру (если локального нет)")
  void testResolve_ContainerDefault() throws Exception {
    String result = invokeResolve("missing.key", "missing.env", null, "container_url");
    assertEquals("container_url", result);
  }

  // ==========================================
  // HELPERS (Reflection)
  // ==========================================

  /**
   * Вызов приватного статического метода resolve
   */
  private String invokeResolve(String sysPropKey, String envKey, String localDefault, String containerDefault) throws Exception {
    Method method = DatabaseConnection.class.getDeclaredMethod("resolve", String.class, String.class, String.class, String.class);
    method.setAccessible(true);
    return (String) method.invoke(null, sysPropKey, envKey, localDefault, containerDefault);
  }

  /**
   * Чтение приватного статического поля (чтобы сохранить настройки)
   */
  private static String getStaticField(String fieldName) throws Exception {
    java.lang.reflect.Field field = DatabaseConnection.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return (String) field.get(null);
  }
}