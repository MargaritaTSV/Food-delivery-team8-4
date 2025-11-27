package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.notification.Notification;
import com.team8.fooddelivery.model.notification.NotificationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final Map<Long, List<Notification>> notifications = new ConcurrentHashMap<>();
    private long notificationIdSeq = 1;

    /** Базовый метод */
    public void notify(Long clientId, NotificationTemplate template, Object... args) {
        Notification notification = Notification.builder()
                .id(notificationIdSeq++)
                .clientId(clientId)
                .type(template.getType())
                .message(template.format(args))
                .timestamp(LocalDateTime.now())
                .build();

        notifications.computeIfAbsent(clientId, k -> new ArrayList<>()).add(notification);

        logger.info("[{}] Notification for client {}: {}", template.getType(), clientId, notification.getMessage());
    }

    // =============================
    // УНИФИЦИРОВАННЫЕ МЕТОДЫ
    // =============================

    /** Account notifications */
    public void notifyAccount(Long clientId, String messageArg) {
        notify(clientId, NotificationTemplate.PROFILE_UPDATED, messageArg);
    }

    public void notifyWelcome(Long clientId, String clientName) {
        notify(clientId, NotificationTemplate.WELCOME_ACCOUNT, clientName);
    }

    /** Order notifications */
    public void notifyOrderPlaced(Long clientId, long orderId, long price) {
        notify(clientId, NotificationTemplate.ORDER_PLACED, orderId, price);
    }

    public void notifyOrderPaid(Long clientId, long orderId) {
        notify(clientId, NotificationTemplate.ORDER_PAID, orderId);
    }

    public void notifyDelivery(Long clientId, long orderId) {
        notify(clientId, NotificationTemplate.ORDER_DELIVERED, orderId);
    }

    // =============================
    // CRUD уведомлений
    // =============================

    public List<Notification> getNotifications(Long clientId) {
        return notifications.getOrDefault(clientId, Collections.emptyList());
    }

    public void clear(Long clientId) {
        notifications.remove(clientId);
        logger.info("Notifications cleared for client {}", clientId);
    }
}
