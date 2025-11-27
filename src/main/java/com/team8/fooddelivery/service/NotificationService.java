package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final Map<Long, List<Notification>> CLIENT_NOTIFICATIONS = new HashMap<>();
    private static Long NOTIF_ID_SEQ = 1L;

    // Базовая отправка
    public Notification sendNotification(Long clientId, String type, String message) {
        Notification notification = new Notification(
                NOTIF_ID_SEQ++,
                clientId,
                type,
                message,
                LocalDateTime.now()
        );
        CLIENT_NOTIFICATIONS.computeIfAbsent(clientId, k -> new ArrayList<>()).add(notification);
        logger.info("Отправлено уведомление клиенту {}: {}", clientId, message);
        return notification;
    }

    // Унифицированные методы
    public void notifyAccount(Long clientId, String message) {
        sendNotification(clientId, "ACCOUNT", message);
    }

    public void notifyOrder(Long clientId, String message) {
        sendNotification(clientId, "ORDER", message);
    }

    public void notifyDelivery(Long clientId, String message) {
        sendNotification(clientId, "DELIVERY", message);
    }

    public List<Notification> getNotifications(Long clientId) {
        return CLIENT_NOTIFICATIONS.getOrDefault(clientId, Collections.emptyList());
    }

    public void printNotifications(Long clientId) {
        List<Notification> list = getNotifications(clientId);
        if (list.isEmpty()) {
            System.out.println("Уведомлений нет");
        } else {
            list.forEach(System.out::println);
        }
    }
}
