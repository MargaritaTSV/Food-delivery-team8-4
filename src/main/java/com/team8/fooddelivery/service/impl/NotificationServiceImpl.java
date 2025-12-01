package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.notification.Notification;
import com.team8.fooddelivery.model.notification.NotificationTemplate;
import com.team8.fooddelivery.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final Map<Long, List<Notification>> notifications = new ConcurrentHashMap<>();
    private long notificationIdSeq = 1;

    /** Базовый метод */
    @Override
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
    @Override
    public void notifyAccount(Long clientId, String messageArg) {
        notify(clientId, NotificationTemplate.PROFILE_UPDATED, messageArg);
    }

    @Override
    public void notifyWelcome(Long clientId, String clientName) {
        notify(clientId, NotificationTemplate.WELCOME_ACCOUNT, clientName);
    }

    /** Order notifications */
    @Override
    public void notifyOrderPlaced(Long clientId, long orderId, long price) {
        notify(clientId, NotificationTemplate.ORDER_PLACED, orderId, price);
    }

    @Override
    public void notifyOrderPaid(Long clientId, long orderId) {
        notify(clientId, NotificationTemplate.ORDER_PAID, orderId);
    }

    @Override
    public void notifyDelivery(Long clientId, long orderId) {
        notify(clientId, NotificationTemplate.ORDER_DELIVERED, orderId);
    }

    // =============================
    // CRUD уведомлений
    // =============================

    @Override
    public List<Notification> getNotifications(Long clientId) {
        return notifications.getOrDefault(clientId, Collections.emptyList());
    }

    @Override
    public void clear(Long clientId) {
        notifications.remove(clientId);
        logger.info("Notifications cleared for client {}", clientId);
    }
}
