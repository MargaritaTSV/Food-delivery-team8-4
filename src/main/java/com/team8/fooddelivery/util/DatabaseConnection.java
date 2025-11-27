package com.team8.fooddelivery.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Утилитный класс для управления подключениями к PostgreSQL базе данных
 */
public class DatabaseConnection {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

  private static final String DEFAULT_URL_CONTAINER = "jdbc:postgresql://db:5432/food_delivery";
  private static final String DEFAULT_URL_LOCAL = "jdbc:postgresql://localhost:5432/food_delivery";
  private static final String DEFAULT_USER = "fooddelivery_user"; // Используем пользователя, которого мы настроили
  private static final String DEFAULT_PASSWORD = "fooddelivery_pass"; // Используем пароль, который мы настроили

  private static String dbUrl = resolve("db.url", "DB_URL", DEFAULT_URL_LOCAL, DEFAULT_URL_CONTAINER);
  private static String dbUser = resolve("db.user", "DB_USER", DEFAULT_USER, DEFAULT_USER);
  private static String dbPassword = resolve("db.password", "DB_PASSWORD", DEFAULT_PASSWORD, DEFAULT_PASSWORD);

  static {
    try {
      Class.forName("org.postgresql.Driver");
      logger.info("PostgreSQL JDBC Driver загружен успешно");
    } catch (ClassNotFoundException e) {
      logger.error("Не удалось загрузить PostgreSQL JDBC Driver", e);
      throw new RuntimeException("PostgreSQL JDBC Driver не найден", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    try {
      Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
      logger.debug("Подключение к БД установлено: {}", dbUrl);
      return connection;
    } catch (SQLException e) {
      logger.error("Ошибка подключения к БД: {}", dbUrl, e);
      throw e;
    }
  }

  public static void setConnectionParams(String url, String user, String password) {
    dbUrl = url;
    dbUser = user;
    dbPassword = password;
    logger.info("Параметры подключения обновлены: url={}, user={}", url, user);
  }

  private static String resolve(String sysPropKey, String envKey, String localDefault, String containerDefault) {
    String fromSystem = System.getProperty(sysPropKey);
    if (fromSystem != null && !fromSystem.isBlank()) {
      logger.info("Используем параметры подключения из системного свойства {}", sysPropKey);
      return fromSystem;
    }

    String fromEnv = System.getenv(envKey);
    if (fromEnv != null && !fromEnv.isBlank()) {
      logger.info("Используем параметры подключения из переменной окружения {}", envKey);
      return fromEnv;
    }

    // По умолчанию сначала пробуем локальный хост, а в контейнерной среде можно переопределить DB_URL
    String fallback = localDefault != null ? localDefault : containerDefault;
    logger.info("Используем параметры подключения по умолчанию: {}", fallback);
    return fallback;
  }

  public static void initializeDatabase() {
    if (!DatabaseInitializer.isDatabaseInitialized()) {
      DatabaseInitializer.initializeDatabase();
    } else {
      logger.info("База данных уже инициализирована");
    }
  }

  public static boolean testConnection() {
    try (Connection conn = getConnection()) {
      return conn != null && !conn.isClosed();
    } catch (SQLException e) {
      logger.error("Тест подключения не пройден", e);
      return false;
    }
  }

  public static void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
        logger.debug("Подключение закрыто");
      } catch (SQLException e) {
        logger.error("Ошибка при закрытии подключения", e);
      }
    }
  }
}
