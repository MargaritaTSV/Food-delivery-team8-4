package com.team8.fooddelivery.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCategoryTest {

  @Test
  @DisplayName("Тест методов values() и valueOf() для покрытия")
  void testEnumCoverage() {
    // 1. Тест метода values()
    // Возвращает массив всех констант. Обязателен для покрытия.
    ProductCategory[] values = ProductCategory.values();

    // Проверяем количество (у вас 5 констант)
    assertEquals(5, values.length);

    // Проверяем наличие конкретных значений
    assertEquals(ProductCategory.BAKERY, values[0]);
    assertEquals(ProductCategory.OTHER, values[4]);

    // 2. Тест метода valueOf()
    // Ищет константу по строке
    ProductCategory category = ProductCategory.valueOf("MAIN_DISH");
    assertEquals(ProductCategory.MAIN_DISH, category);

    // 3. Тест на ошибку (проверка несуществующего значения)
    assertThrows(IllegalArgumentException.class, () -> {
      ProductCategory.valueOf("NON_EXISTENT_CATEGORY");
    });
  }
}