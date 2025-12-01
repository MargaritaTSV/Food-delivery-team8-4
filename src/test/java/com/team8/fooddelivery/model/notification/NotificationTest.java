package com.team8.fooddelivery.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

  @Test
  @DisplayName("Тест Builder и AllArgsConstructor")
  void testBuilderAndAllArgsConstructor() {
    // 1. Подготовка данных
    Long id = 1L;
    Long clientId = 100L;
    String type = "INFO";
    String message = "Your order is ready";
    LocalDateTime now = LocalDateTime.now();

    // 2. Создание объекта через Builder
    Notification notification = Notification.builder()
        .id(id)
        .clientId(clientId)
        .type(type)
        .message(message)
        .timestamp(now)
        .build();

    // 3. Проверка геттеров
    assertNotNull(notification);
    assertEquals(id, notification.getId());
    assertEquals(clientId, notification.getClientId());
    assertEquals(type, notification.getType());
    assertEquals(message, notification.getMessage());
    assertEquals(now, notification.getTimestamp());
  }

  @Test
  @DisplayName("Тест NoArgsConstructor и Setters")
  void testNoArgsConstructorAndSetters() {
    // 1. Создание через пустой конструктор
    Notification notification = new Notification();

    // 2. Установка значений через сеттеры
    Long id = 2L;
    Long clientId = 200L;
    String type = "WARNING";
    String message = "Payment failed";
    LocalDateTime now = LocalDateTime.now();

    notification.setId(id);
    notification.setClientId(clientId);
    notification.setType(type);
    notification.setMessage(message);
    notification.setTimestamp(now);

    // 3. Проверка значений
    assertEquals(id, notification.getId());
    assertEquals(clientId, notification.getClientId());
    assertEquals(type, notification.getType());
    assertEquals(message, notification.getMessage());
    assertEquals(now, notification.getTimestamp());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokStandardMethods() {
    LocalDateTime now = LocalDateTime.now();

    // Создаем два идентичных объекта
    Notification n1 = Notification.builder()
        .id(1L)
        .clientId(10L)
        .type("A")
        .message("Msg")
        .timestamp(now)
        .build();

    Notification n2 = Notification.builder()
        .id(1L)
        .clientId(10L)
        .type("A")
        .message("Msg")
        .timestamp(now)
        .build();

    // Создаем отличный объект (другой ID)
    Notification n3 = Notification.builder()
        .id(2L)
        .clientId(10L)
        .type("A")
        .message("Msg")
        .timestamp(now)
        .build();

    // Тест equals
    assertEquals(n1, n2);       // Равны
    assertNotEquals(n1, n3);    // Не равны
    assertNotEquals(n1, null);  // Не null
    assertNotEquals(n1, new Object()); // Не другой класс

    // Тест hashCode
    assertEquals(n1.hashCode(), n2.hashCode());
    assertNotEquals(n1.hashCode(), n3.hashCode());

    // Тест toString
    String str = n1.toString();
    assertNotNull(str);
    // Проверяем, что в строке есть имя класса и значения полей
    assertTrue(str.contains("Notification"));
    assertTrue(str.contains("id=1"));
    assertTrue(str.contains("type=A"));
    assertTrue(str.contains("Msg"));
  }
}