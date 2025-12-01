package com.team8.fooddelivery.model.client;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Cart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

  @Test
  @DisplayName("Тест Builder и AllArgsConstructor")
  void testBuilderAndAllArgsConstructor() {
    // 1. Подготовка данных
    Long id = 1L;
    String name = "Ivan";
    String phone = "+79001234567";
    String passHash = "hash123";
    String email = "ivan@test.com";
    Instant now = Instant.now();
    List<String> history = new ArrayList<>();
    history.add("Order1");

    // ИСПРАВЛЕНИЕ: Используем builder(), так как new Address() недоступен
    Address address = Address.builder().city("Moscow").build();

    // ИСПРАВЛЕНИЕ: Используем builder() для корзины (или null, если билдера нет)
    // Если Cart.builder() горит красным, замени на: Cart cart = null;
    Cart cart = Cart.builder().id(100L).build();

    // 2. Создаем клиента через Builder
    Client client = Client.builder()
        .id(id)
        .name(name)
        .phone(phone)
        .passwordHash(passHash)
        .email(email)
        .address(address)
        .status(null)
        .createdAt(now)
        .isActive(true)
        .cart(cart)
        .orderHistory(history)
        .build();

    // 3. Проверяем, что поля записались верно
    assertNotNull(client);
    assertEquals(id, client.getId());
    assertEquals(name, client.getName());
    assertEquals(phone, client.getPhone());
    assertEquals(email, client.getEmail());
    assertEquals(address, client.getAddress()); // Проверяем объект адреса
    assertEquals(cart, client.getCart());       // Проверяем объект корзины
    assertEquals(history, client.getOrderHistory());
    assertTrue(client.isActive());
  }

  @Test
  @DisplayName("Тест NoArgsConstructor и Setters")
  void testNoArgsConstructorAndSetters() {
    // 1. Создаем через пустой конструктор
    Client client = new Client();

    // Проверяем дефолтные значения
    assertTrue(client.isActive()); // По умолчанию true
    // Instant.now() присваивается при создании, он не должен быть null
    assertNotNull(client.getCreatedAt());

    // 2. Заполняем через сеттеры
    client.setId(2L);
    client.setName("Petr");
    client.setEmail("p@p.ru");

    // 3. Проверяем геттеры
    assertEquals(2L, client.getId());
    assertEquals("Petr", client.getName());
    assertEquals("p@p.ru", client.getEmail());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokMethods() {
    // Создаем два одинаковых объекта
    Instant time = Instant.now();

    Client c1 = Client.builder().id(1L).name("Test").createdAt(time).build();
    Client c2 = Client.builder().id(1L).name("Test").createdAt(time).build();
    Client c3 = Client.builder().id(2L).name("Diff").createdAt(time).build();

    // Equals & HashCode
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);

    // ToString
    String str = c1.toString();
    assertNotNull(str);
    assertTrue(str.contains("id=1"));
    assertTrue(str.contains("name=Test"));
    assertTrue(str.contains("Client"));
  }
}