package com.team8.fooddelivery.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

  @Test
  @DisplayName("Тест Builder и AllArgsConstructor")
  void testBuilderAndAllArgsConstructor() {
    // 1. Подготовка данных
    Long id = 100L;
    String name = "Pasta";
    String desc = "Delicious";
    Double weight = 350.5;
    Double price = 450.0;
    Duration cookingTime = Duration.ofMinutes(25);
    // Используем null для категории, если Enum не доступен в тесте,
    // или подставьте ProductCategory.MAIN_DISH
    ProductCategory category = null;

    // 2. Создание через Builder
    Product product = Product.builder()
        .productId(id)
        .name(name)
        .description(desc)
        .weight(weight)
        .price(price)
        .category(category)
        .available(false) // Явно меняем значение
        .cookingTimeMinutes(cookingTime)
        .build();

    // 3. Проверка геттеров
    assertNotNull(product);
    assertEquals(id, product.getProductId());
    assertEquals(name, product.getName());
    assertEquals(desc, product.getDescription());
    assertEquals(weight, product.getWeight());
    assertEquals(price, product.getPrice());
    assertEquals(category, product.getCategory());
    assertFalse(product.getAvailable()); // Мы установили false
    assertEquals(cookingTime, product.getCookingTimeMinutes());
  }

  @Test
  @DisplayName("Тест NoArgsConstructor и дефолтных значений")
  void testNoArgsConstructorAndDefaults() {
    // 1. Создание через пустой конструктор
    Product product = new Product();

    // 2. Проверка дефолтного значения
    // В вашем коде: private Boolean available = true;
    // Пустой конструктор должен сохранить это значение.
    assertTrue(product.getAvailable(), "По умолчанию available должно быть true");

    // 3. Проверка сеттеров
    product.setProductId(200L);
    product.setName("Soup");
    product.setDescription("Hot");
    product.setWeight(200.0);
    product.setPrice(150.0);
    product.setCategory(null);
    product.setAvailable(false);
    product.setCookingTimeMinutes(Duration.ofMinutes(10));

    // 4. Проверка значений
    assertEquals(200L, product.getProductId());
    assertEquals("Soup", product.getName());
    assertFalse(product.getAvailable());
    assertEquals(Duration.ofMinutes(10), product.getCookingTimeMinutes());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokStandardMethods() {
    Duration time = Duration.ofMinutes(15);

    // Создаем два одинаковых объекта
    Product p1 = Product.builder()
        .productId(1L)
        .name("Burger")
        .price(100.0)
        .cookingTimeMinutes(time)
        .build();

    Product p2 = Product.builder()
        .productId(1L)
        .name("Burger")
        .price(100.0)
        .cookingTimeMinutes(time)
        .build();

    // Создаем отличающийся объект
    Product p3 = Product.builder()
        .productId(2L)
        .name("Burger")
        .price(100.0)
        .cookingTimeMinutes(time)
        .build();

    // Equals
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, new Object());

    // HashCode
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1.hashCode(), p3.hashCode());

    // ToString
    String str = p1.toString();
    assertNotNull(str);
    assertTrue(str.contains("Product"));
    assertTrue(str.contains("Burger"));
    assertTrue(str.contains("productId=1"));
  }
}