package com.team8.fooddelivery;

import com.team8.fooddelivery.util.DatabaseInitializer;

/**
 * Главный класс приложения для запуска инициализации базы данных
 */
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Запуск инициализации базы данных через встроенный инициализатор...");

        try {
            DatabaseInitializer.initializeDatabase();
            System.out.println("🎉 Инициализация базы данных завершена успешно!");
        } catch (Exception e) {
            System.err.println("💥 Ошибка при инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}