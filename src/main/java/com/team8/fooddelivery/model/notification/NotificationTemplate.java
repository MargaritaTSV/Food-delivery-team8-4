package com.team8.fooddelivery.model.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTemplate {

    WELCOME_ACCOUNT("ACCOUNT", "Добро пожаловать, %s! Ваш аккаунт успешно создан."),
    PROFILE_UPDATED("ACCOUNT", "Профиль обновлён, %s."),
    ORDER_PLACED("ORDER", "Ваш заказ #%d на сумму %d₽ принят."),
    ORDER_PAID("ORDER", "Ваш заказ #%d успешно оплачен."),
    ORDER_DELIVERED("ORDER", "Ваш заказ #%d доставлен. Приятного аппетита!");

    private final String type;
    private final String template;

    public String format(Object... args) {
        return String.format(template, args);
    }
}

