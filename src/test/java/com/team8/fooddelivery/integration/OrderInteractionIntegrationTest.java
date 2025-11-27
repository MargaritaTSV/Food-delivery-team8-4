package com.team8.fooddelivery.integration;

import com.team8.fooddelivery.model.*;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.*;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные сценарии заказов с участием клиентов, магазинов и курьеров")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderInteractionIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(OrderInteractionIntegrationTest.class);

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
  @DisplayName("Клиент оформляет заказ в магазине с полным адресом и товарами")
  void customerCreatesOrderWithAddressAndItems() throws SQLException {
    Long clientId = saveActiveClient("client-addr-" + UUID.randomUUID());
    Long shopId = saveApprovedShop("shop-addr-" + UUID.randomUUID());

    Address deliveryAddress = sampleAddress("Питер", "Невский", "10", "77");
    List<CartItem> items = List.of(
        CartItem.builder().productId(101L).productName("Пицца Маргарита").quantity(1).price(750.0).build(),
        CartItem.builder().productId(102L).productName("Лимонад").quantity(2).price(120.0).build()
    );

    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(clientId);
    order.setRestaurantId(shopId);
    order.setDeliveryAddress(deliveryAddress);
    order.setTotalPrice(items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());
    order.setItems(items);
    order.setCreatedAt(Instant.now());

    log.info("Создаем заказ клиента {} для магазина {}", clientId, shopId);
    Long orderId = orderRepository.save(order);

    Optional<Order> storedOrderOpt = orderRepository.findById(orderId);
    assertTrue(storedOrderOpt.isPresent(), "Заказ клиента не найден");
    Order storedOrder = storedOrderOpt.get();
    assertEquals(orderId, storedOrder.getId());
    assertEquals(order.getTotalPrice(), storedOrder.getTotalPrice());
    assertNotNull(storedOrder.getDeliveryAddress());
    assertEquals(deliveryAddress.getCity(), storedOrder.getDeliveryAddress().getCity());
    assertEquals(2, storedOrder.getItems().size());
  }

  @Test
  @DisplayName("Курьер берет заказ и довозит его до клиента")
  void courierTakesAndCompletesOrder() throws SQLException {
    Long courierId = saveCourierWithStatus(CourierStatus.ON_SHIFT, "bike");
    Long clientId = saveActiveClient("client-courier-" + UUID.randomUUID());
    Long shopId = saveApprovedShop("shop-courier-" + UUID.randomUUID());

    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(clientId);
    order.setRestaurantId(shopId);
    order.setDeliveryAddress(sampleAddress("Москва", "Тверская", "5", "21"));
    order.setTotalPrice(450.0);
    order.setItems(List.of(CartItem.builder()
        .productId(201L)
        .productName("Суп дня")
        .quantity(1)
        .price(450.0)
        .build()));

    log.info("Создаем заказ без курьера для клиента {}", clientId);
    Long orderId = orderRepository.save(order);

    log.info("Назначаем курьера {} на заказ {}", courierId, orderId);
    order.setId(orderId);
    order.setCourierId(courierId);
    order.setStatus(OrderStatus.DELIVERING);
    orderRepository.update(order);

    List<Order> courierOrders = orderRepository.findByCourierId(courierId);
    Optional<Order> delivering = courierOrders.stream()
        .filter(o -> orderId.equals(o.getId()))
        .findFirst();
    assertTrue(delivering.isPresent());
    assertEquals(OrderStatus.DELIVERING, delivering.get().getStatus());

    log.info("Курьер {} завершает доставку заказа {}", courierId, orderId);
    order.setStatus(OrderStatus.COMPLETED);
    orderRepository.update(order);

    Optional<Order> completed = orderRepository.findById(orderId);
    assertTrue(completed.isPresent());
    assertEquals(OrderStatus.COMPLETED, completed.get().getStatus());
  }

  @Test
  @DisplayName("Магазин видит заказы и может обновить цену и статус готовности")
  void shopUpdatesOrderPriceAndStatus() throws SQLException {
    Long clientId = saveActiveClient("client-shop-" + UUID.randomUUID());
    Long shopId = saveApprovedShop("shop-order-" + UUID.randomUUID());

    Order order = new Order();
    order.setStatus(OrderStatus.PENDING);
    order.setCustomerId(clientId);
    order.setRestaurantId(shopId);
    order.setDeliveryAddress(sampleAddress("Казань", "Баумана", "3", "15"));
    order.setTotalPrice(300.0);
    order.setItems(List.of(CartItem.builder()
        .productId(301L)
        .productName("Салат")
        .quantity(1)
        .price(300.0)
        .build()));

    log.info("Магазин {} принимает заказ от клиента {}", shopId, clientId);
    Long orderId = orderRepository.save(order);

    Optional<Order> pending = orderRepository.findById(orderId);
    assertTrue(pending.isPresent());
    assertEquals(OrderStatus.PENDING, pending.get().getStatus());

    log.info("Магазин {} обновляет цену и статус заказа {}", shopId, orderId);
    order.setId(orderId);
    order.setTotalPrice(350.0);
    order.setStatus(OrderStatus.PREPARING);
    orderRepository.update(order);

    Optional<Order> updated = orderRepository.findById(orderId);
    assertTrue(updated.isPresent());
    assertEquals(350.0, updated.get().getTotalPrice());
    assertEquals(OrderStatus.PREPARING, updated.get().getStatus());
  }

  private Long saveActiveClient(String key) throws SQLException {
    Client client = Client.builder()
        .name("Клиент " + key)
        .phone("+7999" + Math.abs(key.hashCode() % 1_000_000))
        .email(key + "@test.com")
        .passwordHash("hash")
        .status(ClientStatus.ACTIVE)
        .isActive(true)
        .orderHistory(List.of())
        .build();
    Long id = clientRepository.save(client);
    log.info("Создан клиент {}", id);
    return id;
  }

  private Long saveApprovedShop(String key) throws SQLException {
    Shop shop = new Shop();
    shop.setNaming("Магазин " + key);
    shop.setEmailForAuth(key + "@shop.test");
    shop.setPhoneForAuth("+7888" + Math.abs(key.hashCode() % 1_000_000));
    shop.setStatus(ShopStatus.APPROVED);
    shop.setPassword("shop_pass");
    Long id = shopRepository.save(shop);
    log.info("Создан магазин {}", id);
    return id;
  }

  private Long saveCourierWithStatus(CourierStatus status, String transport) throws SQLException {
    Courier courier = new Courier();
    courier.setName("Курьер " + status);
    courier.setPhoneNumber("+777" + System.nanoTime() % 1_000_000);
    courier.setPassword("courier_pass");
    courier.setStatus(status);
    courier.setTransportType(transport);
    courier.setCurrentBalance(0.0);
    courier.setBankCard(1111222233334444L);
    Long id = courierRepository.save(courier);
    log.info("Создан курьер {} со статусом {}", id, status);
    return id;
  }

  private Address sampleAddress(String city, String street, String building, String apartment) {
    return Address.builder()
        .country("Россия")
        .city(city)
        .street(street)
        .building(building)
        .apartment(apartment)
        .entrance("1")
        .floor(2)
        .addressNote("Проверка доставки")
        .build();
  }
}
