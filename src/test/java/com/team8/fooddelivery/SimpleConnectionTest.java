package com.team8.fooddelivery;

import com.team8.fooddelivery.util.DatabaseConnection;
import com.team8.fooddelivery.util.DatabaseInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

@DisplayName("Простая проверка подключения к БД")
public class SimpleConnectionTest {

    @Test
    @DisplayName("Проверка подключения к PostgreSQL")
    void testDatabaseConnection() throws SQLException {
        // Настройка параметров подключения
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");

        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        try {
            DatabaseConnection.initializeDatabase();
            System.out.println("✅ База данных успешно инициализирована");
        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации БД: " + e.getMessage());
            throw new RuntimeException("Не удалось инициализировать тестовую БД", e);
        }
        
        // Проверка подключения
        boolean connected = DatabaseConnection.testConnection();
        
        if (!connected) {
            System.err.println("\n❌ ОШИБКА: Не удалось подключиться к базе данных!");
            System.err.println("Проверьте:");
            System.err.println("1. PostgreSQL запущен");
            System.err.println("2. База данных 'food_delivery' создана");
            System.err.println("3. Схема создана (выполнен schema.sql)");
            System.err.println("4. Параметры подключения:");
            System.err.println("   URL: " + dbUrl);
            System.err.println("   User: " + dbUser);
            System.err.println("\nДля изменения параметров используйте системные свойства:");
            System.err.println("   -Ddb.url=... -Ddb.user=... -Ddb.password=...");
        }
        DatabaseInitializer.isDatabaseInitialized();
    }
}