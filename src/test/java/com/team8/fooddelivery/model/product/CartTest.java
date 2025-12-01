package com.team8.fooddelivery.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

  @Test
  @DisplayName("Тест статического метода createEmpty")
  void testCreateEmpty() {
    Long clientId = 100L;

    // Вызываем статический метод
    Cart cart = Cart.createEmpty(clientId);

    // Проверяем, что метод отработал корректно
    assertNotNull(cart);
    assertNull(cart.getId());           // ID должен быть null
    assertEquals(clientId, cart.getClientId());
    assertNotNull(cart.getItems());     // Список должен быть инициализирован
    assertTrue(cart.getItems().isEmpty()); // И он должен быть пустым
  }

  @Test
  @DisplayName("Тест ленивой инициализации getItems (ветка if items == null)")
  void testGetItemsLazyInit() {
    // 1. Создаем объект через конструктор (items по умолчанию null)
    Cart cart = new Cart();

    // 2. Вызываем getItems
    List<CartItem> items = cart.getItems();

    // 3. Проверяем, что список создался (зашли внутрь if)
    assertNotNull(items);
    assertTrue(items.isEmpty());

    // Проверяем, что поле внутри объекта действительно изменилось
    assertSame(items, cart.getItems());
  }

  @Test
  @DisplayName("Тест getItems, когда список уже существует (ветка else/skip if)")
  void testGetItemsExisting() {
    List<CartItem> preCreatedList = new ArrayList<>();
    Cart cart = new Cart();

    // Устанавливаем список вручную
    cart.setItems(preCreatedList);

    // Проверяем, что метод возвращает именно наш список, не создавая новый
    assertSame(preCreatedList, cart.getItems());
  }

  @Test
  @DisplayName("Тест метода getTotalPrice (Бизнес-логика)")
  void testTotalPrice() {
    // 1. Создаем реальные объекты товаров
    // Предполагаем, что у CartItem есть сеттеры или конструктор (Lombok @Data)
    CartItem item1 = new CartItem();
    item1.setPrice(100.50);
    item1.setQuantity(2);
    // Расчет: 100.50 * 2 = 201.0 -> (long) 201

    CartItem item2 = new CartItem();
    item2.setPrice(50.0);
    item2.setQuantity(1);
    // Расчет: 50.0 * 1 = 50.0 -> (long) 50

    // 2. Добавляем их в список
    List<CartItem> items = Arrays.asList(item1, item2);

    // 3. Создаем корзину
    Cart cart = Cart.builder()
        .items(items)
        .build();

    // 4. Проверяем итоговую сумму
    // Ожидаем: 201 + 50 = 251
    assertEquals(251L, cart.getTotalPrice());
  }

  @Test
  @DisplayName("Тест getTotalPrice для пустой корзины")
  void testTotalPriceEmpty() {
    Cart cart = new Cart(); // items = null
    // getItems() создаст пустой список -> stream -> sum = 0
    assertEquals(0L, cart.getTotalPrice());
  }

  @Test
  @DisplayName("Тест Lombok методов (Builder, AllArgsConstructor, Getters, Equals)")
  void testLombokFeatures() {
    Long id = 1L;
    Long clientId = 55L;
    List<CartItem> items = new ArrayList<>();

    // Тест Builder
    Cart c1 = Cart.builder()
        .id(id)
        .clientId(clientId)
        .items(items)
        .build();

    // Тест AllArgsConstructor (через проверку полей) и Getters
    assertEquals(id, c1.getId());
    assertEquals(clientId, c1.getClientId());
    assertEquals(items, c1.getItems());

    // Тест Equals и HashCode
    Cart c2 = Cart.builder().id(id).clientId(clientId).items(items).build();
    Cart c3 = Cart.builder().id(2L).clientId(clientId).items(items).build();

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);

    // Тест toString
    String str = c1.toString();
    assertNotNull(str);
    assertTrue(str.contains("Cart"));
    assertTrue(str.contains("clientId=55"));
  }
}