package com.team8.fooddelivery.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Класс для инициализации структуры базы данных
 */
public class DatabaseInitializer {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

  // Добавляем файл очистки в начало
  private static final List<String> SQL_FILES = Arrays.asList(
      // Сначала создаем все таблицы без внешних ключей
      "sql/001_create_base_tables/001_create_addresses.sql",
      "sql/001_create_base_tables/002_create_working_hours.sql",
      "sql/001_create_base_tables/003_create_clients.sql",
      "sql/002_create_shop_tables/004_create_shops.sql",  // БЕЗ внешних ключей
      "sql/002_create_shop_tables/005_create_products.sql",  // БЕЗ внешних ключей
      "sql/003_create_courier_tables/007_create_courier_tables.sql",
      "sql/004_create_order_tables/008_create_orders.sql",  // БЕЗ внешних ключей
      "sql/004_create_order_tables/009_create_order_items.sql",  // БЕЗ внешних ключей
      "sql/004_create_order_tables/010_create_carts.sql",  // БЕЗ внешних ключей
      "sql/004_create_order_tables/011_create_cart_items.sql",  // БЕЗ внешних ключей
      // Потом добавляем внешние ключи
      "sql/002_create_shop_tables/006_add_shop_foreign_keys.sql",
      "sql/004_create_order_tables/012_add_cart_foreign_keys.sql",
      "sql/005_create_indexes/013_create_indexes.sql"
  );

  /**
   * Инициализирует всю структуру базы данных
   */
  public static void initializeDatabase() {
    if (isDatabaseInitialized()) {
      logger.info("База данных уже инициализирована, повторная загрузка схемы пропущена");
      return;
    }

    logger.info("Начинается инициализация базы данных...");

    try (Connection conn = DatabaseConnection.getConnection()) {
      // Создаем схему если не существует
      //executeSql(conn, "CREATE SCHEMA IF NOT EXISTS food_delivery");

      // Устанавливаем схему по умолчанию для этой сессии
      //executeSql(conn, "SET search_path TO food_delivery, public");

      // Выполняем все SQL файлы в правильном порядке
      for (String sqlFile : SQL_FILES) {
        executeSqlFile(conn, sqlFile);
      }

      logger.info("✅ База данных успешно инициализирована");

    } catch (SQLException e) {
      logger.error("❌ Ошибка инициализации базы данных", e);
      throw new RuntimeException("Не удалось инициализировать базу данных", e);
    }
  }

  /**
   * Выполняет SQL команды с улучшенной обработкой ошибок
   */
  private static void executeSqlStatements(Connection conn, String sql) throws SQLException {
    String[] statements = sql.split(";(?=(?:[^']*'[^']*')*[^']*$)");

    for (String statement : statements) {
      String trimmed = statement.trim();

      if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.startsWith("/*")) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(trimmed);
          logger.debug("Выполнено: {}", trimmed.substring(0, Math.min(50, trimmed.length())) + "...");
        } catch (SQLException e) {
          // Игнорируем только ошибки "уже существует", для остальных - бросаем исключение
          if (e.getMessage().contains("already exists") ||
              e.getMessage().contains("существует") ||
              e.getMessage().contains("exists")) {
            logger.debug("Объект уже существует: {}", e.getMessage());
          } else {
            logger.error("❌ Критическая ошибка выполнения SQL: {}", e.getMessage());
            logger.error("SQL: {}", trimmed);
            throw e;
          }
        }
      }
    }
  }

  // Вспомогательный метод для выполнения одиночных SQL
  private static void executeSql(Connection conn, String sql) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    }
  }

  // Обновите также метод проверки инициализации
  public static boolean isDatabaseInitialized() {
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement()) {

      // Указываем схему явно
      stmt.executeQuery("SELECT 1 FROM addresses LIMIT 1");
      return true;

    } catch (SQLException e) {
      return false;
    }
  }

  private static void executeSqlFile(Connection conn, String filePath) throws SQLException {
    logger.info("Выполнение SQL файла: {}", filePath);

    try (InputStream inputStream = DatabaseInitializer.class.getClassLoader()
        .getResourceAsStream(filePath)) {

      if (inputStream == null) {
        logger.error("Файл не найден: {}", filePath);
        throw new SQLException("SQL файл не найден: " + filePath);
      }

      String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      executeSqlStatements(conn, sql);  // ← вот здесь он вызывает другой метод

      logger.info("✅ Файл выполнен: {}", filePath);

    } catch (Exception e) {
      logger.error("❌ Ошибка выполнения файла: {}", filePath, e);
      throw new SQLException("Ошибка выполнения SQL файла: " + filePath, e);
    }
  }

  public static void refreshDataBase() throws SQLException {
    logger.info("Отчистка данных из таблиц");

    try (Connection conn = DatabaseConnection.getConnection()) {
      executeSqlFile(conn, "sql/000_drop_tables.sql");
    }
  }

  public static void fullCleanDatabase() {
    logger.info("Удаление таблиц из БД...");

    try (Connection conn = DatabaseConnection.getConnection()) {
      // Отключаем auto-commit для выполнения всех команд в одной транзакции
      conn.setAutoCommit(false);

      try (Statement stmt = conn.createStatement()) {
        // Отключаем проверку внешних ключей
        stmt.execute("SET session_replication_role = 'replica'");

        // Удаляем таблицы
        stmt.execute("DROP TABLE IF EXISTS cart_items CASCADE");
        stmt.execute("DROP TABLE IF EXISTS carts CASCADE");
        stmt.execute("DROP TABLE IF EXISTS order_items CASCADE");
        stmt.execute("DROP TABLE IF EXISTS orders CASCADE");
        stmt.execute("DROP TABLE IF EXISTS products CASCADE");
        stmt.execute("DROP TABLE IF EXISTS couriers CASCADE");
        stmt.execute("DROP TABLE IF EXISTS shops CASCADE");
        stmt.execute("DROP TABLE IF EXISTS clients CASCADE");
        stmt.execute("DROP TABLE IF EXISTS working_hours CASCADE");
        stmt.execute("DROP TABLE IF EXISTS addresses CASCADE");

        // Включаем проверку внешних ключей обратно
        stmt.execute("SET session_replication_role = 'origin'");

        // Коммитим транзакцию
        conn.commit();
        logger.info("✅ Таблицы успешно удалены");

      } catch (SQLException e) {
        // Откатываем транзакцию при ошибке
        conn.rollback();
        throw e;
      } finally {
        // Восстанавливаем auto-commit
        conn.setAutoCommit(true);
      }

    } catch (SQLException e) {
      logger.error("❌ Ошибка удаления таблиц из БД: {}", e.getMessage());
      // Игнорируем ошибки "таблица не существует"
      if (!e.getMessage().contains("does not exist")) {
        throw new RuntimeException("Не удалось удалить таблицы зи БД", e);
      }
      logger.info("✅ Таблицы уже удалены или не существовали");
    }
  }
}
/*
  public static void cleanDatabase() {
    logger.info("Очистка базы данных...");

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement()) {

      // Отключаем проверку внешних ключей
      stmt.execute("SET session_replication_role = 'replica'");

      // Удаляем таблицы в правильном порядке (от дочерних к родительским)
      String[] tables = {
          "cart_items", "carts", "order_items", "orders",
          "products", "couriers", "shops", "clients",
          "working_hours", "addresses"
      };

      for (String table : tables) {
        try {
          stmt.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
          logger.debug("✅ Таблица {} удалена", table);
        } catch (SQLException e) {
          logger.warn("⚠️ Не удалось удалить таблицу {}: {}", table, e.getMessage());
        }
      }

      // Включаем проверку внешних ключей обратно
      stmt.execute("SET session_replication_role = 'origin'");

      logger.info("✅ База данных успешно очищена");

    } catch (SQLException e) {
      logger.error("❌ Ошибка очистки базы данных", e);
      throw new RuntimeException("Не удалось очистить базу данных", e);
    }
  } */