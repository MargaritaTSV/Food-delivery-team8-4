package com.team8.fooddelivery.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLoggerTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  private SimpleLogger logger;

  @BeforeEach
  void setUp() {
    // Перехватываем стандартный вывод в наши потоки
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    // Инициализируем логгер (передаем любой класс, например этот же)
    logger = new SimpleLogger(SimpleLoggerTest.class);
  }

  @AfterEach
  void tearDown() {
    // ОБЯЗАТЕЛЬНО возвращаем все как было, иначе другие тесты ничего не напишут в консоль
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  @DisplayName("Тест уровня INFO")
  void testInfo() {
    String msg = "Test Info Message";
    logger.info(msg);

    // Превращаем захваченные байты в строку
    String output = outContent.toString();

    // Проверяем, что вывод содержит нужные части
    // Мы не проверяем точное время, так как оно меняется каждую миллисекунду
    assertTrue(output.contains("[INFO]"), "Output should contain [INFO]: " + output);
    assertTrue(output.contains(msg), "Output should contain message: " + output);
    // Проверяем формат времени хотя бы частично (наличие года и тире)
    // Формат: yyyy-MM-dd HH:mm:ss
    // Используем более гибкое регулярное выражение
    assertTrue(output.matches(".*\\d{4}-\\d{2}-\\d{2}.*") || output.contains("2024") || output.contains("2025"), 
               "Output should contain date format: " + output);
  }

  @Test
  @DisplayName("Тест уровня WARN")
  void testWarn() {
    String msg = "Warning happening!";
    logger.warn(msg);

    String output = outContent.toString();

    assertTrue(output.contains("[WARN]"));
    assertTrue(output.contains(msg));
  }

  @Test
  @DisplayName("Тест уровня ERROR (пишет в System.err)")
  void testError() {
    String msg = "Critical Error";
    logger.error(msg);

    // ВАЖНО: Error пишет в errContent, а не в outContent
    String output = errContent.toString();

    assertTrue(output.contains("[ERROR]"));
    assertTrue(output.contains(msg));
  }
}