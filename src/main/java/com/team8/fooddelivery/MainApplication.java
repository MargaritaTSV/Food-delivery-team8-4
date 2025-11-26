package com.team8.fooddelivery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Главный класс приложения для запуска инициализации базы данных
 */
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Запуск инициализации базы данных через скрипт...");

        try {
            runSchemeScript();
            System.out.println("🎉 Инициализация базы данных завершена успешно!");
        } catch (Exception e) {
            System.err.println("💥 Ошибка при инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Запускает bash-скрипт для инициализации схемы БД
     */
    public static void runSchemeScript() throws IOException, InterruptedException {
        String scriptPath = "./run_scheme.sh";

        // Проверяем существование скрипта
        ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath);

        // Перенаправляем вывод скрипта в консоль Java
        processBuilder.redirectErrorStream(true);

        System.out.println("📁 Запуск скрипта: " + scriptPath);

        Process process = processBuilder.start();

        // Читаем вывод скрипта
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // Ждем завершения скрипта
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Скрипт завершился с ошибкой, код: " + exitCode);
        }
    }
}