package com.team8.fooddelivery.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

  @Test
  @DisplayName("Тест Builder и AllArgsConstructor")
  void testBuilderAndAllArgsConstructor() {
    // 1. Подготовка данных
    Long id = 1L;
    Long cartId = 10L;
    Long productId = 100L;
    String productName = "Pizza";
    int quantity = 2;
    double price = 500.0;

    // 2. Создание через Builder
    CartItem item = CartItem.builder()
        .id(id)
        .cartId(cartId)
        .productId(productId)
        .productName(productName)
        .quantity(quantity)
        .price(price)
        .build();

    // 3. Проверка геттеров
    assertNotNull(item);
    assertEquals(id, item.getId());
    assertEquals(cartId, item.getCartId());
    assertEquals(productId, item.getProductId());
    assertEquals(productName, item.getProductName());
    assertEquals(quantity, item.getQuantity());
    assertEquals(price, item.getPrice());
  }

  @Test
  @DisplayName("Тест NoArgsConstructor и Setters")
  void testNoArgsConstructorAndSetters() {
    // 1. Создание пустого объекта
    CartItem item = new CartItem();

    // 2. Установка значений через сеттеры
    item.setId(2L);
    item.setCartId(20L);
    item.setProductId(200L);
    item.setProductName("Burger");
    item.setQuantity(1);
    item.setPrice(300.50);

    // 3. Проверка значений
    assertEquals(2L, item.getId());
    assertEquals(20L, item.getCartId());
    assertEquals("Burger", item.getProductName());
    assertEquals(300.50, item.getPrice());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokStandardMethods() {
    // Создаем два одинаковых объекта
    CartItem item1 = CartItem.builder()
        .id(1L)
        .cartId(10L)
        .productId(100L)
        .productName("Pizza")
        .quantity(2)
        .price(500.0)
        .build();

    CartItem item2 = CartItem.builder()
        .id(1L)
        .cartId(10L)
        .productId(100L)
        .productName("Pizza")
        .quantity(2)
        .price(500.0)
        .build();

    // Создаем отличный объект (другой ID и цена)
    CartItem item3 = CartItem.builder()
        .id(999L)
        .cartId(10L)
        .productId(100L)
        .productName("Pizza")
        .quantity(2)
        .price(999.0)
        .build();

    // Тест equals
    assertEquals(item1, item2);       // Должны быть равны
    assertNotEquals(item1, item3);    // Должны быть не равны
    assertNotEquals(item1, null);     // Не равен null
    assertNotEquals(item1, new Object()); // Не равен другому типу

    // Тест hashCode
    assertEquals(item1.hashCode(), item2.hashCode());
    assertNotEquals(item1.hashCode(), item3.hashCode());

    // Тест toString
    String str = item1.toString();
    assertNotNull(str);
    assertTrue(str.contains("CartItem"));
    assertTrue(str.contains("Pizza"));
    assertTrue(str.contains("500.0"));
  }
}