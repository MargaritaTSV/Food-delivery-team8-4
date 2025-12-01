package com.team8.fooddelivery.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTypeTest {

  @Test
  @DisplayName("Тест методов values() и valueOf() для покрытия")
  void testEnumCoverage() {
    // 1. Тест метода values()
    // Этот метод возвращает массив всех констант. Его вызов обязателен для coverage.
    NotificationType[] values = NotificationType.values();

    // Проверяем количество констант
    assertEquals(5, values.length);

    // Проверяем наличие конкретных значений в массиве (опционально)
    assertEquals(NotificationType.ACCOUNT_REGISTERED, values[0]);
    assertEquals(NotificationType.ORDER_DELIVERED, values[4]);

    // 2. Тест метода valueOf()
    // Этот метод ищет константу по строковому имени.
    NotificationType type = NotificationType.valueOf("ORDER_CREATED");
    assertEquals(NotificationType.ORDER_CREATED, type);

    // 3. Тест на ошибку (для уверенности, что это тот самый Enum)
    assertThrows(IllegalArgumentException.class, () -> {
      NotificationType.valueOf("NON_EXISTENT_TYPE");
    });
  }
}