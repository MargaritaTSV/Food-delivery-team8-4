package com.team8.fooddelivery;

import com.team8.fooddelivery.util.DatabaseInitializer;
import com.team8.fooddelivery.util.WebAppExtractor;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.core.StandardContext;

import java.nio.file.Path;

/**
 * Главный класс приложения для запуска инициализации базы данных
 */
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Запуск инициализации базы данных через встроенный инициализатор...");

        try {
            DatabaseInitializer.initializeDatabase();
            System.out.println("🎉 Инициализация базы данных завершена успешно!");

            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            Path webAppDir = WebAppExtractor.extractWebAppToTemp();

            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.getConnector();

            Context context = tomcat.addWebapp("", webAppDir.toString());
            ((StandardContext) context).setReloadable(false);

            tomcat.start();
            System.out.printf("🌐 Приложение запущено на http://localhost:%d/%n", port);
            tomcat.getServer().await();
        } catch (Exception e) {
            System.err.println("💥 Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}