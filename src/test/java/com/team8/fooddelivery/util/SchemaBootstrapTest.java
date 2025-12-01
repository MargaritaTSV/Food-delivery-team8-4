package com.team8.fooddelivery.util;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты миграции схемы (SchemaBootstrap)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchemaBootstrapTest {

  // Настройки для локального запуска (IntelliJ -> localhost)
  private static final String REAL_URL = "jdbc:postgresql://localhost:5432/food_delivery";
  private static final String REAL_USER = "fooddelivery_user";
  private static final String REAL_PASS = "fooddelivery_pass";

  @BeforeEach
  void setup() {
    DatabaseConnection.setConnectionParams(REAL_URL, REAL_USER, REAL_PASS);
  }

  @Test
  @Order(1)
  @DisplayName("1. Если таблицы orders нет — ничего не падает")
  void testTableMissing() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // 1. Удаляем таблицу orders, чтобы смоделировать ситуацию
      stmt.execute("DROP TABLE IF EXISTS orders CASCADE");

      // 2. Запускаем метод
      assertDoesNotThrow(() -> SchemaBootstrap.ensureOrderColumns(conn));

      // Лог должен написать "orders table is missing", но исключения быть не должно
    }
  }

  @Test
  @Order(2)
  @DisplayName("2. Добавление колонок в 'голую' таблицу")
  void testAddMissingColumns() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // 1. Создаем "бедную" таблицу orders (только ID, без нужных колонок)
      stmt.execute("DROP TABLE IF EXISTS orders CASCADE");
      stmt.execute("CREATE TABLE orders (id SERIAL PRIMARY KEY)");

      // Проверяем, что колонки 'delivery_address' реально НЕТ
      assertThrows(SQLException.class, () -> stmt.execute("SELECT delivery_address FROM orders"));

      // 2. Запускаем bootstrap
      SchemaBootstrap.ensureOrderColumns(conn);

      // 3. Проверяем, что колонки ПОЯВИЛИСЬ
      // Если колонки нет, этот запрос упадет. Если есть — пройдет успешно.
      assertDoesNotThrow(() -> stmt.execute("SELECT delivery_address, payment_status, payment_method, estimated_delivery_time FROM orders"));
    }
  }

  @Test
  @Order(3)
  @DisplayName("3. Если колонки уже есть — повторный запуск безопасен")
  void testColumnsAlreadyExist() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
      // Таблица уже полная после теста №2

      // Запускаем еще раз
      assertDoesNotThrow(() -> SchemaBootstrap.ensureOrderColumns(conn));

      // Если бы код пытался добавить колонку снова, упала бы ошибка "column already exists"
    }
  }

  @Test
  @Order(4)
  @DisplayName("4. Передача null соединения")
  void testNullConnection() {
    assertDoesNotThrow(() -> SchemaBootstrap.ensureOrderColumns(null));
  }

  @Test
  @Order(5)
  @DisplayName("5. Ошибка SQL (симуляция закрытого соединения)")
  void testSqlException() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.close(); // Закрываем соединение специально

      // Метод должен поймать SQLException внутри и просто залогировать warning
      assertDoesNotThrow(() -> SchemaBootstrap.ensureOrderColumns(conn));
    }
  }
}