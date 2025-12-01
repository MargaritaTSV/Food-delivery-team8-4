package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.notification.Notification;
import com.team8.fooddelivery.model.notification.NotificationTemplate;

import java.util.List;

/**
 * Интерфейс для работы с уведомлениями клиентов
 */
public interface NotificationService {

    /**
     * Базовый метод для отправки уведомления
     * @param clientId ID клиента
     * @param template Шаблон уведомления
     * @param args Аргументы для форматирования шаблона
     */
    void notify(Long clientId, NotificationTemplate template, Object... args);

    /**
     * Уведомление об обновлении профиля
     * @param clientId ID клиента
     * @param messageArg Сообщение
     */
    void notifyAccount(Long clientId, String messageArg);

    /**
     * Уведомление о приветствии
     * @param clientId ID клиента
     * @param clientName Имя клиента
     */
    void notifyWelcome(Long clientId, String clientName);

    /**
     * Уведомление о размещении заказа
     * @param clientId ID клиента
     * @param orderId ID заказа
     * @param price Цена заказа
     */
    void notifyOrderPlaced(Long clientId, long orderId, long price);

    /**
     * Уведомление об оплате заказа
     * @param clientId ID клиента
     * @param orderId ID заказа
     */
    void notifyOrderPaid(Long clientId, long orderId);

    /**
     * Уведомление о доставке заказа
     * @param clientId ID клиента
     * @param orderId ID заказа
     */
    void notifyDelivery(Long clientId, long orderId);

    /**
     * Получить все уведомления клиента
     * @param clientId ID клиента
     * @return Список уведомлений
     */
    List<Notification> getNotifications(Long clientId);

    /**
     * Очистить уведомления клиента
     * @param clientId ID клиента
     */
    void clear(Long clientId);
}
