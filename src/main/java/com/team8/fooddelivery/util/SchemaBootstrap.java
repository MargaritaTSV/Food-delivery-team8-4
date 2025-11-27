package com.team8.fooddelivery.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Выполняет безболезненный прогон DDL, чтобы гарантировать наличие новых колонок
 * в уже существующей таблице orders. Никаких таблиц не удаляется, добавляются
 * только отсутствующие столбцы.
 */
public final class SchemaBootstrap {
  private static final Logger logger = LoggerFactory.getLogger(SchemaBootstrap.class);

  private SchemaBootstrap() {
  }

  public static void ensureOrderColumns(Connection connection) {
    if (connection == null) {
      return;
    }

    try {
      DatabaseMetaData metaData = connection.getMetaData();
      if (!tableExists(metaData, "orders")) {
        logger.warn("orders table is missing; run schema creation before starting the app");
        return;
      }

      ensureColumn(connection, metaData, "orders", "delivery_address", "TEXT");
      ensureColumn(connection, metaData, "orders", "payment_status", "VARCHAR(50)");
      ensureColumn(connection, metaData, "orders", "payment_method", "VARCHAR(50)");
      ensureColumn(connection, metaData, "orders", "estimated_delivery_time", "TIMESTAMP");
    } catch (SQLException e) {
      logger.warn("Could not validate schema for orders table", e);
    }
  }

  private static void ensureColumn(Connection connection, DatabaseMetaData metaData, String tableName,
      String columnName, String columnDefinition) throws SQLException {
    try (var rs = metaData.getColumns(null, null, tableName, columnName)) {
      if (rs.next()) {
        return; // столбец уже существует
      }
    }

    String ddl = String.format("ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s %s", tableName, columnName,
        columnDefinition);
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(ddl);
      logger.info("Добавлен отсутствующий столбец {}.{}", tableName, columnName);
    }
  }

  private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
    try (var rs = metaData.getTables(null, null, tableName, null)) {
      return rs.next();
    }
  }
}
