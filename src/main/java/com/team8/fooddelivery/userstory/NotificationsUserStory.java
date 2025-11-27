package com.team8.fooddelivery.userstory;

import com.team8.fooddelivery.model.notification.Notification;
import com.team8.fooddelivery.service.impl.NotificationServiceImpl;
import com.team8.fooddelivery.util.DatabaseInitializer;

import java.util.List;

public class NotificationsUserStory {

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        NotificationServiceImpl notificationService = new NotificationServiceImpl();
        Long clientId = 1L;

        // ==== 1. Отправка уведомлений готовыми шаблонами ====
        notificationService.notifyWelcome(clientId, "Иван");
        notificationService.notifyOrderPlaced(clientId, 101, 1890);
        notificationService.notifyOrderPaid(clientId, 101);
        notificationService.notifyDelivery(clientId, 101);

        // ==== 2. Получение уведомлений из реализованного сервиса ====
        List<Notification> notifications = notificationService.getNotifications(clientId);
        System.out.println("Уведомления клиента " + clientId + ":");
        notifications.forEach(System.out::println);

        // ==== 3. Проверка количества уведомлений ====
        if (notifications.size() == 4) {
            System.out.println("Тест пройден: все уведомления отправлены");
        } else {
            System.out.println("Тест провален: ожидалось 4 уведомления, но получили " + notifications.size());
        }

        // ==== 4. Очистка уведомлений реальным методом clear ====
        notificationService.clear(clientId);
        System.out.println("После очистки:");
        notificationService.getNotifications(clientId).forEach(System.out::println);
    }
}
