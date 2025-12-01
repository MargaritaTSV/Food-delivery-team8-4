package com.team8.fooddelivery.model.shop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Тесты для перечисления ShopType, обеспечивающие полное покрытие кода (100% Line Coverage).
 * Поскольку Enum - это простая структура, тесты фокусируются на проверке существования
 * всех констант, их количества и корректности преобразования из строки.
 */
public class ShopTypeTest {

  private final int EXPECTED_COUNT = 7;

  /**
   * Тест проверяет, что количество объявленных констант соответствует ожидаемому.
   * Это покрывает линию, на которой объявлено само перечисление и все константы.
   */
  @Test
  void testEnumValuesCount() {
    ShopType[] allTypes = ShopType.values();
    assertEquals(EXPECTED_COUNT, allTypes.length, "Ожидалось " + EXPECTED_COUNT + " констант в ShopType.");
  }

  /**
   * Тест проверяет, что все объявленные константы существуют и имеют правильные имена.
   * Это гарантирует, что каждая константа в Enum была "вызвана" тестом.
   */
  @Test
  void testAllValuesAreDefined() {
    Set<String> actualNames = new HashSet<>();
    for (ShopType type : ShopType.values()) {
      actualNames.add(type.name());
    }

    Set<String> expectedNames = new HashSet<>(Arrays.asList(
        "RESTAURANT", "CAFE", "BAKERY", "GROCERY", "PHARMACY", "OTHER", "FAST_FOOD"
    ));

    assertEquals(expectedNames, actualNames, "Объявленные имена констант не совпадают с ожидаемыми.");
  }

  /**
   * Тест проверяет корректность статического метода valueOf(String name).
   * Это покрывает логику преобразования строки в константу.
   */
  @Test
  void testValueOf() {
    // Проверка корректного преобразования
    assertEquals(ShopType.RESTAURANT, ShopType.valueOf("RESTAURANT"), "valueOf должен вернуть RESTAURANT.");
    assertEquals(ShopType.FAST_FOOD, ShopType.valueOf("FAST_FOOD"), "valueOf должен вернуть FAST_FOOD.");

    // Проверка случая, когда константа не найдена (ожидается IllegalArgumentException)
    assertThrows(IllegalArgumentException.class, () -> {
      ShopType.valueOf("NON_EXISTENT_TYPE");
    }, "Попытка получить несуществующую константу должна вызвать исключение.");
  }
}