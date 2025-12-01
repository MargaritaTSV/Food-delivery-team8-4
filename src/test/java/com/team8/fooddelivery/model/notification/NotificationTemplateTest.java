package com.team8.fooddelivery.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateTest {

  @Test
  @DisplayName("Тест значений полей (Getters) и типов уведомлений")
  void testEnumFields() {
    // Проверяем WELCOME_ACCOUNT
    NotificationTemplate welcome = NotificationTemplate.WELCOME_ACCOUNT;
    assertEquals("ACCOUNT", welcome.getType());
    assertTrue(welcome.getTemplate().contains("Добро пожаловать"));

    // Проверяем ORDER_PLACED
    NotificationTemplate orderPlaced = NotificationTemplate.ORDER_PLACED;
    assertEquals("ORDER", orderPlaced.getType());
    assertTrue(orderPlaced.getTemplate().contains("заказ #%d"));
  }

  @Test
  @DisplayName("Тест метода format()")
  void testFormatMethod() {
    // 1. Тест с подстановкой строки (%s)
    String welcomeMsg = NotificationTemplate.WELCOME_ACCOUNT.format("Ivan");
    assertEquals("Добро пожаловать, Ivan! Ваш аккаунт успешно создан.", welcomeMsg);

    // 2. Тест с подстановкой строки при обновлении профиля
    String profileMsg = NotificationTemplate.PROFILE_UPDATED.format("данные изменены");
    assertEquals("Профиль обновлён, данные изменены.", profileMsg);

    // 3. Тест с числами (Long/Integer %d) - Заказ создан
    // Шаблон: "Ваш заказ #%d на сумму %d₽ принят."
    String orderPlacedMsg = NotificationTemplate.ORDER_PLACED.format(101, 2500);
    assertEquals("Ваш заказ #101 на сумму 2500₽ принят.", orderPlacedMsg);

    // 4. Тест с одним числом - Заказ оплачен
    // Шаблон: "Ваш заказ #%d успешно оплачен."
    String orderPaidMsg = NotificationTemplate.ORDER_PAID.format(101);
    assertEquals("Ваш заказ #101 успешно оплачен.", orderPaidMsg);

    // 5. Тест доставки
    String deliveredMsg = NotificationTemplate.ORDER_DELIVERED.format(101);
    assertEquals("Ваш заказ #101 доставлен. Приятного аппетита!", deliveredMsg);
  }

  @Test
  @DisplayName("Тест стандартных методов Enum (values, valueOf)")
  void testStandardEnumMethods() {
    // Тест values() - проверяем, что все константы присутствуют
    NotificationTemplate[] values = NotificationTemplate.values();
    assertEquals(5, values.length); // У нас 5 констант

    // Тест valueOf() - поиск по имени
    NotificationTemplate template = NotificationTemplate.valueOf("ORDER_DELIVERED");
    assertNotNull(template);
    assertEquals(NotificationTemplate.ORDER_DELIVERED, template);
    assertEquals("ORDER", template.getType());

    // Проверка на несуществующее значение (опционально, но полезно)
    assertThrows(IllegalArgumentException.class, () -> {
      NotificationTemplate.valueOf("UNKNOWN_TYPE");
    });
  }
}