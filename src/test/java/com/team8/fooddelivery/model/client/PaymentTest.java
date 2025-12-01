package com.team8.fooddelivery.model.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

  @Test
  @DisplayName("Тест Builder и AllArgsConstructor")
  void testBuilderAndAllArgsConstructor() {
    // 1. Подготовка данных
    Long id = 100L;
    Long orderId = 555L;
    Double amount = 1500.50;
    Instant now = Instant.now();

    // Предполагаем, что это Enum-ы. Если нет - подставьте нужные объекты.
    PaymentMethodForOrder method = null; // Можно заменить на PaymentMethodForOrder.CARD
    PaymentStatus status = null;         // Можно заменить на PaymentStatus.PAID

    // 2. Использование Builder (это также тестирует AllArgsConstructor внутри)
    Payment payment = Payment.builder()
        .id(id)
        .orderId(orderId)
        .amount(amount)
        .method(method)
        .status(status)
        .createdAt(now)
        .build();

    // 3. Проверка Геттеров
    assertNotNull(payment);
    assertEquals(id, payment.getId());
    assertEquals(orderId, payment.getOrderId());
    assertEquals(amount, payment.getAmount());
    assertEquals(method, payment.getMethod());
    assertEquals(status, payment.getStatus());
    assertEquals(now, payment.getCreatedAt());
  }

  @Test
  @DisplayName("Тест NoArgsConstructor и Setters")
  void testNoArgsConstructorAndSetters() {
    // 1. Создание через пустой конструктор
    Payment payment = new Payment();

    // 2. Использование сеттеров
    payment.setId(200L);
    payment.setOrderId(777L);
    payment.setAmount(99.99);
    payment.setMethod(null);
    payment.setStatus(null);
    payment.setCreatedAt(Instant.MIN);

    // 3. Проверка значений (повторная проверка геттеров)
    assertEquals(200L, payment.getId());
    assertEquals(777L, payment.getOrderId());
    assertEquals(99.99, payment.getAmount());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokMethods() {
    Instant now = Instant.now();

    // Создаем два идентичных объекта
    Payment p1 = Payment.builder().id(1L).orderId(10L).amount(100.0).createdAt(now).build();
    Payment p2 = Payment.builder().id(1L).orderId(10L).amount(100.0).createdAt(now).build();

    // Создаем отличный объект
    Payment p3 = Payment.builder().id(2L).orderId(20L).amount(200.0).createdAt(now).build();

    // Тест equals
    assertEquals(p1, p2);       // Должны быть равны
    assertNotEquals(p1, p3);    // Должны быть не равны
    assertNotEquals(p1, null);  // Не должен быть равен null
    assertNotEquals(p1, new Object()); // Не должен быть равен объекту другого класса

    // Тест hashCode
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1.hashCode(), p3.hashCode());

    // Тест toString
    String str = p1.toString();
    assertNotNull(str);
    // Проверяем, что toString содержит имена полей и их значения
    assertTrue(str.contains("Payment"));
    assertTrue(str.contains("id=1"));
    assertTrue(str.contains("amount=100.0"));
  }
}