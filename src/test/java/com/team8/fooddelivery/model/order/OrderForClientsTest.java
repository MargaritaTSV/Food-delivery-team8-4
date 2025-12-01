package com.team8.fooddelivery.model.order;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderForClientsTest {

  @Test
  @DisplayName("Тест AllArgsConstructor и Getters")
  void testAllArgsConstructorAndGetters() {
    // 1. Подготовка данных
    Long orderId = 101L;
    Long shopId = 55L;
    Long clientId = 777L;
    List<Product> items = new ArrayList<>(); // Пустой список для теста
    Address address = null; // Можно подставить mock или объект, если есть
    OrderStatus status = null; // Предполагаем Enum
    Double total = 1500.0;
    Double revenue = 1400.0;
    Duration orderTime = Duration.ofMinutes(5);
    Duration estTime = Duration.ofMinutes(30);
    Duration actTime = Duration.ofMinutes(25);
    String custNotes = "No onions";
    String intNotes = "VIP client";
    String reason = "N/A";
    Duration prepTime = Duration.ofMinutes(20);
    boolean isPaid = true;
    PaymentMethodForOrder method = null; // Предполагаем Enum

    // 2. Создание объекта (У вас нет Builder, используем конструктор)
    OrderForClients order = new OrderForClients(
        orderId, shopId, clientId, items, address, status,
        total, revenue, orderTime, estTime, actTime,
        custNotes, intNotes, reason, prepTime, isPaid, method
    );

    // 3. Проверка геттеров
    assertEquals(orderId, order.getOrderId());
    assertEquals(shopId, order.getShopId());
    assertEquals(clientId, order.getClientId());
    assertEquals(items, order.getItems());
    assertEquals(address, order.getDeliveryAddress());
    assertEquals(status, order.getStatus());
    assertEquals(total, order.getTotalAmount());
    assertEquals(revenue, order.getStoreRevenue());
    assertEquals(orderTime, order.getOrderTime());
    assertEquals(estTime, order.getEstimatedReadyTime());
    assertEquals(actTime, order.getActualReadyTime());
    assertEquals(custNotes, order.getCustomerNotes());
    assertEquals(intNotes, order.getInternalNotes());
    assertEquals(reason, order.getRejectionReason());
    assertEquals(prepTime, order.getPreparationTime());
    assertTrue(order.isPaid()); // getter для boolean - isPaid()
    assertEquals(method, order.getPaymentMethod());
  }

  @Test
  @DisplayName("Тест Setters")
  void testSetters() {
    // 1. Создаем объект с null-значениями (так как нет пустого конструктора, передаем nulls)
    OrderForClients order = new OrderForClients(
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, false, null
    );

    // 2. Устанавливаем новые значения через сеттеры
    Long newId = 999L;
    Duration newDuration = Duration.ofHours(1);

    order.setOrderId(newId);
    order.setShopId(2L);
    order.setClientId(3L);
    order.setItems(new ArrayList<>());
    order.setDeliveryAddress(null);
    order.setStatus(null);
    order.setTotalAmount(100.0);
    order.setStoreRevenue(90.0);
    order.setOrderTime(newDuration);
    order.setEstimatedReadyTime(newDuration);
    order.setActualReadyTime(newDuration);
    order.setCustomerNotes("Note");
    order.setInternalNotes("Internal");
    order.setRejectionReason("None");
    order.setPreparationTime(newDuration);
    order.setPaid(true); // обратите внимание: сеттер называется setPaid
    order.setPaymentMethod(null);

    // 3. Проверяем, что сеттеры сработали
    assertEquals(newId, order.getOrderId());
    assertEquals(2L, order.getShopId());
    assertEquals("Note", order.getCustomerNotes());
    assertTrue(order.isPaid());
    assertEquals(newDuration, order.getPreparationTime());
  }

  @Test
  @DisplayName("Тест equals, hashCode и toString")
  void testLombokStandardMethods() {
    // Создаем два одинаковых объекта
    OrderForClients o1 = new OrderForClients(1L, 1L, 1L, null, null, null, 10.0, 10.0,
        Duration.ZERO, Duration.ZERO, Duration.ZERO, "A", "A", "A", Duration.ZERO, true, null);

    OrderForClients o2 = new OrderForClients(1L, 1L, 1L, null, null, null, 10.0, 10.0,
        Duration.ZERO, Duration.ZERO, Duration.ZERO, "A", "A", "A", Duration.ZERO, true, null);

    // Создаем отличающийся объект (другой ID и другая сумма)
    OrderForClients o3 = new OrderForClients(2L, 1L, 1L, null, null, null, 20.0, 10.0,
        Duration.ZERO, Duration.ZERO, Duration.ZERO, "A", "A", "A", Duration.ZERO, true, null);

    // Equals
    assertEquals(o1, o2);
    assertNotEquals(o1, o3);
    assertNotEquals(o1, null);
    assertNotEquals(o1, new Object());

    // HashCode
    assertEquals(o1.hashCode(), o2.hashCode());
    assertNotEquals(o1.hashCode(), o3.hashCode()); // Скорее всего будут разные

    // ToString
    String str = o1.toString();
    assertNotNull(str);
    assertTrue(str.contains("OrderForClients"));
    assertTrue(str.contains("orderId=1"));
    assertTrue(str.contains("isPaid=true"));
  }
}