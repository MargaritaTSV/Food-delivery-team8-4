package com.team8.fooddelivery.model.shop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса WorkingHours, обеспечивающие высокое покрытие кода
 * путем проверки всех сгенерированных Lombok методов (@Data, @AllArgsConstructor).
 */
public class WorkingHoursTest {

  private final String MONDAY_HOURS = "09:00-18:00";
  private final String TUESDAY_HOURS = "09:00-18:00";
  private final String WEEKEND_HOURS = "10:00-16:00";
  private final String CLOSED = "Closed";

  /**
   * Тестирует конструктор со всеми аргументами и все геттеры.
   * Покрывает инициализацию полей и методы чтения.
   */
  @Test
  void testAllArgsConstructorAndGetters() {
    WorkingHours hours = new WorkingHours(
        MONDAY_HOURS,
        TUESDAY_HOURS,
        "09:00-18:00",
        "09:00-18:00",
        "09:00-18:00",
        WEEKEND_HOURS,
        WEEKEND_HOURS
    );

    // Проверка геттеров
    assertEquals(MONDAY_HOURS, hours.getMonday(), "Геттер понедельника должен вернуть правильное значение.");
    assertEquals(TUESDAY_HOURS, hours.getTuesday(), "Геттер вторника должен вернуть правильное значение.");
    assertEquals("09:00-18:00", hours.getWednesday());
    assertEquals("09:00-18:00", hours.getThursday());
    assertEquals("09:00-18:00", hours.getFriday());
    assertEquals(WEEKEND_HOURS, hours.getSaturday(), "Геттер субботы должен вернуть правильное значение.");
    assertEquals(WEEKEND_HOURS, hours.getSunday(), "Геттер воскресенья должен вернуть правильное значение.");
  }

  /**
   * Тестирует все сеттеры (генерируемые @Data).
   * Покрывает методы записи.
   */
  @Test
  void testSetters() {
    WorkingHours hours = new WorkingHours(CLOSED, CLOSED, CLOSED, CLOSED, CLOSED, CLOSED, CLOSED);

    hours.setMonday(MONDAY_HOURS);
    hours.setTuesday(TUESDAY_HOURS);
    hours.setWednesday(WEEKEND_HOURS);
    hours.setThursday(WEEKEND_HOURS);
    hours.setFriday(MONDAY_HOURS);
    hours.setSaturday(TUESDAY_HOURS);
    hours.setSunday(WEEKEND_HOURS);

    // Проверка, что сеттеры сработали
    assertEquals(MONDAY_HOURS, hours.getMonday());
    assertEquals(TUESDAY_HOURS, hours.getTuesday());
    assertEquals(WEEKEND_HOURS, hours.getWednesday());
    assertEquals(WEEKEND_HOURS, hours.getThursday());
    assertEquals(MONDAY_HOURS, hours.getFriday());
    assertEquals(TUESDAY_HOURS, hours.getSaturday());
    assertEquals(WEEKEND_HOURS, hours.getSunday());
  }

  /**
   * Тестирует сгенерированный Lombok метод toString().
   */
  @Test
  void testToString() {
    WorkingHours hours = new WorkingHours(MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, CLOSED, CLOSED);
    String expected = "WorkingHours(monday=" + MONDAY_HOURS + ", tuesday=" + MONDAY_HOURS + ", wednesday=" + MONDAY_HOURS + ", thursday=" + MONDAY_HOURS + ", friday=" + MONDAY_HOURS + ", saturday=" + CLOSED + ", sunday=" + CLOSED + ")";

    // Проверяем, что toString() не пуст и содержит ожидаемые данные
    assertNotNull(hours.toString());
    assertEquals(expected, hours.toString());
  }

  /**
   * Тестирует сгенерированные Lombok методы equals() и hashCode().
   * Проверяет равенство, неравенство, рефлексивность и согласованность хэш-кода.
   */
  @Test
  void testEqualsAndHashCode() {
    WorkingHours hours1 = new WorkingHours(MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, CLOSED, CLOSED);
    WorkingHours hours2 = new WorkingHours(MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, CLOSED, CLOSED);
    WorkingHours differentHours = new WorkingHours(CLOSED, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, MONDAY_HOURS, CLOSED, CLOSED);

    // Рефлексивность (x.equals(x))
    assertTrue(hours1.equals(hours1));

    // Равенство (x.equals(y))
    assertTrue(hours1.equals(hours2));

    // Неравенство (x.equals(z))
    assertFalse(hours1.equals(differentHours));
    assertFalse(hours1.equals(null));
    assertFalse(hours1.equals("String")); // Проверка равенства с другим типом объекта

    // Согласованность хэш-кода (если equals, то и hashCode)
    assertEquals(hours1.hashCode(), hours2.hashCode());
    assertNotEquals(hours1.hashCode(), differentHours.hashCode());

    // Проверка, что изменение поля влияет на equals/hashCode
    hours2.setMonday(CLOSED);
    assertFalse(hours1.equals(hours2));
    assertNotEquals(hours1.hashCode(), hours2.hashCode());
  }
}