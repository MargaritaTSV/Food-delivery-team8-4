package com.team8.fooddelivery.model;

import com.team8.fooddelivery.model.client.ClientStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса AuthResponse, обеспечивающие высокое покрытие кода
 * путем проверки всех сгенерированных Lombok методов (@Data, @AllArgsConstructor)
 * и работы с enum ClientStatus.
 */
public class AuthResponseTest {

  private final Long CLIENT_ID = 100L;
  private final String AUTH_TOKEN = "a-secure-token-12345";
  private final ClientStatus ACTIVE_STATUS = ClientStatus.ACTIVE;
  private final ClientStatus INACTIVE_STATUS = ClientStatus.INACTIVE; // Заменили PENDING на INACTIVE

  /**
   * Тестирует конструктор со всеми аргументами и все геттеры.
   */
  @Test
  void testAllArgsConstructorAndGetters() {
    AuthResponse response = new AuthResponse(CLIENT_ID, AUTH_TOKEN, ACTIVE_STATUS);

    // Проверка геттеров
    assertEquals(CLIENT_ID, response.getClientId(), "ClientId должен быть корректным.");
    assertEquals(AUTH_TOKEN, response.getAuthToken(), "AuthToken должен быть корректным.");
    assertEquals(ACTIVE_STATUS, response.getStatus(), "Status должен быть корректным.");
  }

  /**
   * Тестирует все сеттеры (генерируемые @Data).
   */
  @Test
  void testSetters() {
    // Инициализация с INACTIVE_STATUS
    AuthResponse response = new AuthResponse(0L, "", INACTIVE_STATUS);

    // Присвоение новых значений через сеттеры
    response.setClientId(CLIENT_ID);
    response.setAuthToken(AUTH_TOKEN);
    response.setStatus(ACTIVE_STATUS);

    // Проверка, что сеттеры сработали
    assertEquals(CLIENT_ID, response.getClientId());
    assertEquals(AUTH_TOKEN, response.getAuthToken());
    assertEquals(ACTIVE_STATUS, response.getStatus());
  }

  /**
   * Тестирует сгенерированный Lombok метод toString().
   */
  @Test
  void testToString() {
    AuthResponse response = new AuthResponse(CLIENT_ID, AUTH_TOKEN, ACTIVE_STATUS);
    String expected = "AuthResponse(clientId=" + CLIENT_ID + ", authToken=" + AUTH_TOKEN + ", status=" + ACTIVE_STATUS + ")";

    // Проверяем, что toString() корректно сформирован
    assertEquals(expected, response.toString());
  }

  /**
   * Тестирует сгенерированные Lombok методы equals() и hashCode().
   */
  @Test
  void testEqualsAndHashCode() {
    AuthResponse response1 = new AuthResponse(CLIENT_ID, AUTH_TOKEN, ACTIVE_STATUS);
    AuthResponse response2 = new AuthResponse(CLIENT_ID, AUTH_TOKEN, ACTIVE_STATUS);
    AuthResponse differentToken = new AuthResponse(CLIENT_ID, "different-token", ACTIVE_STATUS);
    // Используем INACTIVE_STATUS для проверки неравенства
    AuthResponse differentStatus = new AuthResponse(CLIENT_ID, AUTH_TOKEN, INACTIVE_STATUS);

    // Равенство (x.equals(y)) и согласованность хэш-кода
    assertTrue(response1.equals(response2));
    assertEquals(response1.hashCode(), response2.hashCode());

    // Неравенство по токену
    assertFalse(response1.equals(differentToken));
    assertNotEquals(response1.hashCode(), differentToken.hashCode());

    // Неравенство по статусу
    assertFalse(response1.equals(differentStatus));
    assertNotEquals(response1.hashCode(), differentStatus.hashCode());

    // Прочие проверки
    assertFalse(response1.equals(null));
    assertFalse(response1.equals(new Object()));
  }
}