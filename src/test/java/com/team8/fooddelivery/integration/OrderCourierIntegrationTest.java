package com.team8.fooddelivery.integration;

import com.team8.fooddelivery.model.*;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.*;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты заказов и курьеров")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderCourierIntegrationTest {

  private CourierRepository courierRepository;
  private ClientRepository clientRepository;
  private ShopRepository shopRepository;
  private OrderRepository orderRepository;

  @BeforeAll
  static void setupDatabaseConnection() {
    String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
    String dbUser = System.getProperty("db.user", "fooddelivery_user");
    String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
    DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

    if (!DatabaseConnection.testConnection()) {
      throw new RuntimeException("Не удалось подключиться к базе данных");
    }
  }

  @BeforeEach
  void setUp() {
    courierRepository = new CourierRepository();
    clientRepository = new ClientRepository();
    shopRepository = new ShopRepository();
    orderRepository = new OrderRepository();
  }

  @Test
  @DisplayName("Создание курьера и заказа")
  void testCourierAndOrder() throws SQLException {
    // 1. Создание курьера
    Courier courier = new Courier();
    String uniqueCourierPhone = "+799944455" + System.nanoTime() % 100000;
    courier.setName("Тестовый Курьер");
    courier.setPhoneNumber(uniqueCourierPhone);
    courier.setPassword("courier_password_hash");
    courier.setStatus(CourierStatus.OFF_SHIFT);
    courier.setTransportType("bike");
    courier.setCurrentBalance(0.0);
    courier.setBankCard(1234567890123456L);

    Long courierId = courierRepository.save(courier);
    assertNotNull(courierId);

    // 2. Создание клиента для заказа
    String uniqueClientPhone = "+799977788" + System.nanoTime() % 100000;
    String uniqueClientEmail = "orderclient" + System.nanoTime() + "@test.com";
    Client client = Client.builder()
        .name("Клиент для Заказа")
        .phone(uniqueClientPhone)
        .email(uniqueClientEmail)
        .passwordHash("hash")
        .status(ClientStatus.ACTIVE)
        .isActive(true)
        .orderHistory(List.of())
        .build();
    Long clientId = clientRepository.save(client);

    // 3. Создание магазина для заказа
    String uniqueOrderShopPhone = "+799955566" + System.nanoTime() % 100000;
    String uniqueOrderShopEmail = "shop_order" + System.nanoTime() + "@test.com";
    Shop shop = new Shop();
    shop.setNaming("Магазин для Заказа");
    shop.setEmailForAuth(uniqueOrderShopEmail);
    shop.setPhoneForAuth(uniqueOrderShopPhone);
    shop.setStatus(ShopStatus.APPROVED);
    shop.setPassword("shop_pass");
    Long shopId = shopRepository.save(shop);

    // 4. Создание заказа
    Address deliveryAddress = Address.builder()
        .country("Россия")
        .city("Санкт-Петербург")
        .street("Тестовая")
        .building("1")
        .apartment("12")
        .entrance("2")
        .floor(3)
        .addressNote("Позвонить за 5 минут")
        .build();

    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(clientId);
    order.setRestaurantId(shopId);
    order.setDeliveryAddress(deliveryAddress);
    order.setTotalPrice(1000.0);
    order.setItems(List.of(
        CartItem.builder().productId(1L).productName("Пицца Маргарита").quantity(1).price(700.0).build(),
        CartItem.builder().productId(2L).productName("Кола").quantity(2).price(150.0).build()
    ));

    Long orderId = orderRepository.save(order);
    assertNotNull(orderId);

    // 5. Назначение курьера на заказ
    order.setId(orderId);
    order.setCourierId(courierId);
    order.setStatus(OrderStatus.DELIVERING);
    orderRepository.update(order);

    Optional<Order> retrievedOrder = orderRepository.findById(orderId);
    assertTrue(retrievedOrder.isPresent());
    assertEquals(courierId, retrievedOrder.get().getCourierId());
    assertEquals(OrderStatus.DELIVERING, retrievedOrder.get().getStatus());
  }

  @Test
  @DisplayName("Изменение статусов заказа")
  void testOrderStatusFlow() throws SQLException {
    // Создание клиента
    String uniqueClientPhone = "+799966677" + System.nanoTime() % 100000;
    Client client = Client.builder()
        .name("Клиент Статус")
        .phone(uniqueClientPhone)
        .email("statusclient" + System.nanoTime() + "@test.com")
        .passwordHash("hash")
        .status(ClientStatus.ACTIVE)
        .isActive(true)
        .orderHistory(List.of())
        .build();
    Long clientId = clientRepository.save(client);

    // Создание магазина
    Shop shop = new Shop();
    shop.setNaming("Магазин Статус");
    shop.setEmailForAuth("status_shop" + System.nanoTime() + "@test.com");
    shop.setPhoneForAuth("+7999" + System.nanoTime() % 100000);
    shop.setStatus(ShopStatus.APPROVED);
    shop.setPassword("pass");
    Long shopId = shopRepository.save(shop);

    // Создание заказа
    Address statusAddress = Address.builder()
        .country("Россия")
        .city("Москва")
        .street("Статусная")
        .building("5")
        .apartment("7")
        .entrance("1")
        .floor(2)
        .addressNote("Домофон 12")
        .build();

    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(clientId);
    order.setRestaurantId(shopId);
    order.setDeliveryAddress(statusAddress);
    order.setTotalPrice(750.0);
    order.setItems(List.of(
        CartItem.builder().productId(3L).productName("Бургер").quantity(1).price(500.0).build(),
        CartItem.builder().productId(4L).productName("Фри").quantity(1).price(250.0).build()
    ));

    Long orderId = orderRepository.save(order);

    // Проверка начального статуса
    Optional<Order> initialOrder = orderRepository.findById(orderId);
    assertTrue(initialOrder.isPresent());
    assertEquals(OrderStatus.PENDING, initialOrder.get().getStatus());

    // Изменение статуса на PREPARING
    order.setId(orderId);
    order.setStatus(OrderStatus.PREPARING);
    orderRepository.update(order);

    Optional<Order> preparingOrder = orderRepository.findById(orderId);
    assertTrue(preparingOrder.isPresent());
    assertEquals(OrderStatus.PREPARING, preparingOrder.get().getStatus());

    // Изменение статуса на DELIVERED
    order.setStatus(OrderStatus.COMPLETED);
    orderRepository.update(order);

    Optional<Order> deliveredOrder = orderRepository.findById(orderId);
    assertTrue(deliveredOrder.isPresent());
    assertEquals(OrderStatus.COMPLETED, deliveredOrder.get().getStatus());
  }

  @Test
  @DisplayName("Создание курьера с разными статусами")
  void testCourierStatuses() throws SQLException {
    // Курьер OFF_SHIFT
    Courier offShiftCourier = new Courier();
    offShiftCourier.setName("Курьер Отдых");
    offShiftCourier.setPhoneNumber("+7999888" + System.nanoTime() % 100000);
    offShiftCourier.setPassword("pass1");
    offShiftCourier.setStatus(CourierStatus.OFF_SHIFT);
    offShiftCourier.setTransportType("car");
    Long courierId1 = courierRepository.save(offShiftCourier);

    // Курьер ON_SHIFT
    Courier onShiftCourier = new Courier();
    onShiftCourier.setName("Курьер Работа");
    onShiftCourier.setPhoneNumber("+7999889" + System.nanoTime() % 100000);
    onShiftCourier.setPassword("pass2");
    onShiftCourier.setStatus(CourierStatus.ON_SHIFT);
    onShiftCourier.setTransportType("bike");
    Long courierId2 = courierRepository.save(onShiftCourier);

    // Проверка статусов
    Optional<Courier> retrievedOffShift = courierRepository.findById(courierId1);
    Optional<Courier> retrievedOnShift = courierRepository.findById(courierId2);

    assertTrue(retrievedOffShift.isPresent());
    assertTrue(retrievedOnShift.isPresent());
    assertEquals(CourierStatus.OFF_SHIFT, retrievedOffShift.get().getStatus());
    assertEquals(CourierStatus.ON_SHIFT, retrievedOnShift.get().getStatus());
  }
}