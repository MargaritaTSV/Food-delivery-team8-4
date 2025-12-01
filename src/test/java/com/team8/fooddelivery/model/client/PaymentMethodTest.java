package com.team8.fooddelivery.model.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {

  @Test
  @DisplayName("Тест ручного конструктора (Custom Constructor)")
  void testCustomConstructor() {
    // 1. Данные для конструктора
    Long clientId = 100L;
    String type = "DEBIT";
    String cardNum = "1234-5678";
    String holder = "John Doe";
    String expiry = "12/25";
    String cvv = "123";

    // 2. Вызов ручного конструктора
    PaymentMethod pm = new PaymentMethod(clientId, type, cardNum, holder, expiry, cvv);

    // 3. Проверки
    assertNull(pm.getPaymentMethodId()); // ID не передавался, должен быть null
    assertEquals(clientId, pm.getClientId());
    assertEquals(type, pm.getMethodType());
    assertEquals(cardNum, pm.getCardNumber());
    assertEquals(holder, pm.getCardHolderName());
    assertEquals(expiry, pm.getExpiryDate());
    assertEquals(cvv, pm.getCvv());

    // Проверка дефолтных значений, установленных внутри конструктора
    assertFalse(pm.getIsDefault()); // Должно быть false
    assertTrue(pm.getIsActive());   // Должно быть true
    assertNotNull(pm.getCreatedAt()); // Время должно быть установлено
  }

  @Test
  @DisplayName("Тест Lombok: NoArgsConstructor и дефолтные значения полей")
  void testNoArgsConstructorAndDefaults() {
    // 1. Создание через пустой конструктор
    PaymentMethod pm = new PaymentMethod();

    // 2. Проверка инициализации полей (дефолтные значения в объявлении полей)
    assertNotNull(pm);
    assertNull(pm.getPaymentMethodId());

    // isDefault = false при инициализации поля
    assertEquals(Boolean.FALSE, pm.getIsDefault());

    // isActive = true при инициализации поля
    assertEquals(Boolean.TRUE, pm.getIsActive());

    // createdAt = Instant.now() при инициализации поля
    assertNotNull(pm.getCreatedAt());
  }

  @Test
  @DisplayName("Тест Lombok: AllArgsConstructor")
  void testAllArgsConstructor() {
    Instant now = Instant.now();

    // Вызов конструктора со всеми параметрами (генерируется Lombok)
    PaymentMethod pm = new PaymentMethod(
        1L,
        2L,
        "CREDIT",
        "1111",
        "Name",
        "01/30",
        "999",
        true,  // isDefault
        now,
        false  // isActive
    );

    assertEquals(1L, pm.getPaymentMethodId());
    assertEquals(2L, pm.getClientId());
    assertEquals("CREDIT", pm.getMethodType());
    assertTrue(pm.getIsDefault());
    assertEquals(now, pm.getCreatedAt());
    assertFalse(pm.getIsActive());
  }

  @Test
  @DisplayName("Тест Lombok: Setters")
  void testSetters() {
    PaymentMethod pm = new PaymentMethod();

    pm.setPaymentMethodId(5L);
    pm.setClientId(10L);
    pm.setMethodType("PayPal");
    pm.setCardNumber("0000");
    pm.setCardHolderName("Test");
    pm.setExpiryDate("10/24");
    pm.setCvv("111");
    pm.setIsDefault(true);
    pm.setIsActive(false);

    Instant time = Instant.now();
    pm.setCreatedAt(time);

    assertEquals(5L, pm.getPaymentMethodId());
    assertEquals("PayPal", pm.getMethodType());
    assertEquals(time, pm.getCreatedAt());
  }

  @Test
  @DisplayName("Тест Lombok: equals, hashCode и toString")
  void testLombokStandardMethods() {
    Instant now = Instant.now();

    // Два одинаковых объекта
    PaymentMethod pm1 = new PaymentMethod(1L, 1L, "A", "1", "N", "D", "C", false, now, true);
    PaymentMethod pm2 = new PaymentMethod(1L, 1L, "A", "1", "N", "D", "C", false, now, true);

    // Отличающийся объект
    PaymentMethod pm3 = new PaymentMethod(2L, 1L, "A", "1", "N", "D", "C", false, now, true);

    // Equals
    assertEquals(pm1, pm2);
    assertNotEquals(pm1, pm3);
    assertNotEquals(pm1, null);
    assertNotEquals(pm1, new Object());

    // HashCode
    assertEquals(pm1.hashCode(), pm2.hashCode());
    assertNotEquals(pm1.hashCode(), pm3.hashCode());

    // ToString
    String str = pm1.toString();
    assertNotNull(str);
    assertTrue(str.contains("PaymentMethod")); // Имя класса
    assertTrue(str.contains("paymentMethodId=1")); // Значение поля
    assertTrue(str.contains("methodType=A"));
  }
}