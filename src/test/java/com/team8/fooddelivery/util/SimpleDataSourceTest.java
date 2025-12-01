package com.team8.fooddelivery.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDataSourceTest {

  // Используем настройки твоей локальной базы (Docker)
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/food_delivery";
  private static final String DB_USER = "fooddelivery_user";
  private static final String DB_PASS = "fooddelivery_pass";

  @Test
  @DisplayName("Успешное создание соединения и проверка счетчика")
  void testGetConnection_Success() throws SQLException {
    // 1. Создаем DataSource
    SimpleDataSource dataSource = new SimpleDataSource(DB_URL, DB_USER, DB_PASS);

    // Проверяем начальное состояние
    assertEquals(0, dataSource.getActiveConnections(), "Изначально соединений должно быть 0");

    // 2. Получаем соединение
    try (Connection connection = dataSource.getConnection()) {
      assertNotNull(connection);
      assertFalse(connection.isClosed());

      // 3. Проверяем, что счетчик увеличился
      assertEquals(1, dataSource.getActiveConnections());
    }

    // Примечание: В твоем коде нет логики уменьшения счетчика при закрытии,
    // поэтому даже после try-with-resources он останется 1.
    assertEquals(1, dataSource.getActiveConnections());
  }

  @Test
  @DisplayName("Ошибка подключения (Неверный пароль)")
  void testGetConnection_Fail() {
    // Указываем неверные данные
    SimpleDataSource dataSource = new SimpleDataSource(DB_URL, "wrong_user", "wrong_pass");

    // Ожидаем ошибку SQL
    assertThrows(SQLException.class, dataSource::getConnection);

    // ВАЖНЫЙ МОМЕНТ ПО ТВОЕМУ КОДУ:
    // У тебя написано:
    // activeConnections++;
    // return DriverManager.getConnection(...);
    //
    // Это значит, что счетчик увеличивается ДО попытки подключения.
    // Даже если подключение упало, счетчик станет 1.
    assertEquals(1, dataSource.getActiveConnections());
  }
}