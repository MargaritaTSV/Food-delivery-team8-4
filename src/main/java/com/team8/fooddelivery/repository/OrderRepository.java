package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {
  private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
  private final AddressRepository addressRepository = new AddressRepository();
  private volatile Boolean deliveryAddressColumnPresent = null;
  private volatile Boolean paymentStatusColumnPresent = null;
  private volatile Boolean paymentMethodColumnPresent = null;
  private volatile Boolean estimatedDeliveryTimeColumnPresent = null;
  private volatile boolean paymentStatusWarned = false;
  private volatile boolean paymentMethodWarned = false;
  private volatile boolean estimatedDeliveryTimeWarned = false;

  public Long save(Order order) throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
      boolean hasDeliveryAddressColumn = hasDeliveryAddressColumn(conn);
      boolean hasPaymentStatusColumn = hasPaymentStatusColumn(conn);
      boolean hasPaymentMethodColumn = hasPaymentMethodColumn(conn);
      boolean hasEstimatedDeliveryTimeColumn = hasEstimatedDeliveryTimeColumn(conn);

      StringBuilder columns = new StringBuilder(
          "status, customer_id, restaurant_id, delivery_address_id");
      if (hasDeliveryAddressColumn) {
        columns.append(", delivery_address");
      }
      columns.append(", courier_id, total_price");
      if (hasPaymentStatusColumn) {
        columns.append(", payment_status");
      }
      if (hasPaymentMethodColumn) {
        columns.append(", payment_method");
      }
      if (hasEstimatedDeliveryTimeColumn) {
        columns.append(", estimated_delivery_time");
      }
      columns.append(", created_at");

      StringBuilder placeholders = new StringBuilder("?, ?, ?, ?");
      if (hasDeliveryAddressColumn) {
        placeholders.append(", ?");
      }
      placeholders.append(", ?, ?");
      if (hasPaymentStatusColumn) {
        placeholders.append(", ?");
      }
      if (hasPaymentMethodColumn) {
        placeholders.append(", ?");
      }
      if (hasEstimatedDeliveryTimeColumn) {
        placeholders.append(", ?");
      }
      placeholders.append(", ?");

      String sql = "INSERT INTO orders (" + columns + ") VALUES (" + placeholders + ") RETURNING id";

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {

        Long deliveryAddressId = resolveDeliveryAddressId(order.getDeliveryAddressId(), order.getDeliveryAddress());
        String deliveryAddressText = buildDeliveryAddressText(order.getDeliveryAddress());
        order.setDeliveryAddressId(deliveryAddressId);

        stmt.setString(1, order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name());
        stmt.setObject(2, order.getCustomerId(), Types.BIGINT);
        stmt.setObject(3, order.getRestaurantId(), Types.BIGINT);
        stmt.setObject(4, deliveryAddressId, Types.BIGINT);

        int paramIndex = 5;
        if (hasDeliveryAddressColumn) {
          stmt.setString(paramIndex++, deliveryAddressText);
        }

        stmt.setObject(paramIndex++, order.getCourierId(), Types.BIGINT);
        stmt.setObject(paramIndex++, order.getTotalPrice(), Types.DOUBLE);
        if (hasPaymentStatusColumn) {
          stmt.setString(paramIndex++,
              order.getPaymentStatus() != null ? order.getPaymentStatus().name() : PaymentStatus.PENDING.name());
        }
        if (hasPaymentMethodColumn) {
          stmt.setString(paramIndex++, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        }
        if (hasEstimatedDeliveryTimeColumn) {
          stmt.setTimestamp(paramIndex++,
              order.getEstimatedDeliveryTime() != null ? Timestamp.from(order.getEstimatedDeliveryTime()) : null);
        }
        stmt.setTimestamp(paramIndex,
            order.getCreatedAt() != null ? Timestamp.from(order.getCreatedAt()) : Timestamp.from(java.time.Instant.now()));

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          Long id = rs.getLong("id");

          // Сохраняем элементы заказа
          if (order.getItems() != null && !order.getItems().isEmpty()) {
            saveOrderItems(id, order.getItems(), conn);
          }

          logger.debug("Заказ сохранен с id={}", id);
          return id;
        }
        throw new SQLException("Не удалось сохранить заказ");
      }
    }
  }

  public Optional<Order> findById(Long id) throws SQLException {
    String sql = "SELECT * FROM orders WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToOrder(rs, conn));
      }
      return Optional.empty();
    }
  }

  public List<Order> findByCustomerId(Long customerId) throws SQLException {
    String sql = "SELECT * FROM orders WHERE customer_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, customerId);
      ResultSet rs = stmt.executeQuery();

      List<Order> orders = new ArrayList<>();
      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs, conn));
      }
      return orders;
    }
  }

  public List<Order> findByCourierId(Long courierId) throws SQLException {
    String sql = "SELECT * FROM orders WHERE courier_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, courierId);
      ResultSet rs = stmt.executeQuery();

      List<Order> orders = new ArrayList<>();
      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs, conn));
      }
      return orders;
    }
  }

  public List<Order> findByStatus(OrderStatus status) throws SQLException {
    String sql = "SELECT * FROM orders WHERE status = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, status.name());
      ResultSet rs = stmt.executeQuery();

      List<Order> orders = new ArrayList<>();
      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs, conn));
      }
      return orders;
    }
  }

  public List<Order> findAll() throws SQLException {
    String sql = "SELECT * FROM orders";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<Order> orders = new ArrayList<>();
      while (rs.next()) {
        orders.add(mapResultSetToOrder(rs, conn));
      }
      return orders;
    }
  }

  public void update(Order order) throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
      boolean hasDeliveryAddressColumn = hasDeliveryAddressColumn(conn);
      boolean hasPaymentStatusColumn = hasPaymentStatusColumn(conn);
      boolean hasPaymentMethodColumn = hasPaymentMethodColumn(conn);
      boolean hasEstimatedDeliveryTimeColumn = hasEstimatedDeliveryTimeColumn(conn);

      StringBuilder sqlBuilder = new StringBuilder("UPDATE orders SET status=?, customer_id=?, restaurant_id=?, delivery_address_id=?, ");
      if (hasDeliveryAddressColumn) {
        sqlBuilder.append("delivery_address=?, ");
      }
      sqlBuilder.append("courier_id=?, total_price=?, ");
      if (hasPaymentStatusColumn) {
        sqlBuilder.append("payment_status=?, ");
      }
      if (hasPaymentMethodColumn) {
        sqlBuilder.append("payment_method=?, ");
      }
      if (hasEstimatedDeliveryTimeColumn) {
        sqlBuilder.append("estimated_delivery_time=?, ");
      }
      sqlBuilder.append("updated_at=CURRENT_TIMESTAMP WHERE id=?");

      String sql = sqlBuilder.toString();

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {

        Long deliveryAddressId = resolveDeliveryAddressId(order.getDeliveryAddressId(), order.getDeliveryAddress());
        String deliveryAddressText = buildDeliveryAddressText(order.getDeliveryAddress());

        stmt.setString(1, order.getStatus() != null ? order.getStatus().name() : OrderStatus.PENDING.name());
        stmt.setObject(2, order.getCustomerId(), Types.BIGINT);
        stmt.setObject(3, order.getRestaurantId(), Types.BIGINT);
        stmt.setObject(4, deliveryAddressId, Types.BIGINT);

        int paramIndex = 5;
        if (hasDeliveryAddressColumn) {
          stmt.setString(paramIndex++, deliveryAddressText);
        }

        stmt.setObject(paramIndex++, order.getCourierId(), Types.BIGINT);
        stmt.setObject(paramIndex++, order.getTotalPrice(), Types.DOUBLE);
        if (hasPaymentStatusColumn) {
          stmt.setString(paramIndex++,
              order.getPaymentStatus() != null ? order.getPaymentStatus().name() : PaymentStatus.PENDING.name());
        }
        if (hasPaymentMethodColumn) {
          stmt.setString(paramIndex++, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        }
        if (hasEstimatedDeliveryTimeColumn) {
          stmt.setTimestamp(paramIndex++,
              order.getEstimatedDeliveryTime() != null ? Timestamp.from(order.getEstimatedDeliveryTime()) : null);
        }
        stmt.setLong(paramIndex, order.getId());

        stmt.executeUpdate();
        logger.debug("Заказ обновлен: id={}", order.getId());
      }
    }
  }

  public void delete(Long id) throws SQLException {
    String sql = "DELETE FROM orders WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      stmt.executeUpdate();
      logger.debug("Заказ удален: id={}", id);
    }
  }

  private Order mapResultSetToOrder(ResultSet rs, Connection conn) throws SQLException {
    Order order = new Order();
    order.setId(rs.getLong("id"));
    order.setStatus(OrderStatus.valueOf(rs.getString("status")));
    order.setCustomerId(rs.getObject("customer_id", Long.class));
    order.setRestaurantId(rs.getObject("restaurant_id", Long.class));
    order.setDeliveryAddressId(rs.getObject("delivery_address_id", Long.class));
    order.setCourierId(rs.getObject("courier_id", Long.class));

    double totalPriceValue = rs.getDouble("total_price");
    if (rs.wasNull()) {
      order.setTotalPrice(null);
    } else {
      order.setTotalPrice(totalPriceValue);
    }

    if (order.getDeliveryAddressId() != null) {
      addressRepository.findById(order.getDeliveryAddressId()).ifPresent(order::setDeliveryAddress);
    } else if (hasDeliveryAddressColumn(conn)) {
      String rawAddress = rs.getString("delivery_address");
      if (rawAddress != null) {
        order.setDeliveryAddress(addressFromText(rawAddress));
      }
    }
    if (hasPaymentStatusColumn(conn)) {
      order.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
    } else if (!paymentStatusWarned) {
      paymentStatusWarned = true;
      logger.warn("payment_status column is missing in orders table; defaulting to PENDING");
      order.setPaymentStatus(PaymentStatus.PENDING);
    }

    if (hasPaymentMethodColumn(conn)) {
      String paymentMethod = rs.getString("payment_method");
      order.setPaymentMethod(paymentMethod != null ? PaymentMethodForOrder.valueOf(paymentMethod) : null);
    } else if (!paymentMethodWarned) {
      paymentMethodWarned = true;
      logger.warn("payment_method column is missing in orders table; consider running the latest schema scripts");
    }
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      order.setCreatedAt(createdAt.toInstant());
    }
    if (hasEstimatedDeliveryTimeColumn(conn)) {
      Timestamp eta = rs.getTimestamp("estimated_delivery_time");
      if (eta != null) {
        order.setEstimatedDeliveryTime(eta.toInstant());
      }
    } else if (!estimatedDeliveryTimeWarned) {
      estimatedDeliveryTimeWarned = true;
      logger.warn("estimated_delivery_time column is missing in orders table; consider running the latest schema scripts");
    }

    // Загружаем элементы заказа
    List<CartItem> items = findOrderItemsByOrderId(order.getId(), conn);
    order.setItems(items);

    return order;
  }

  private List<CartItem> findOrderItemsByOrderId(Long orderId, Connection conn) throws SQLException {
    String sql = "SELECT product_id, product_name, quantity, price FROM order_items WHERE order_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, orderId);
      ResultSet rs = stmt.executeQuery();

      List<CartItem> items = new ArrayList<>();
      while (rs.next()) {
        items.add(CartItem.builder()
            .productId(rs.getObject("product_id", Long.class))
            .productName(rs.getString("product_name"))
            .quantity(rs.getInt("quantity"))
            .price(rs.getDouble("price"))
            .build());
      }
      return items;
    }
  }

  private Long resolveDeliveryAddressId(Long existingAddressId, Address address) throws SQLException {
    if (existingAddressId != null) {
      return existingAddressId;
    }
    if (address == null) {
      return null;
    }
    Long savedId = addressRepository.save(address);
    return savedId;
  }

  private String buildDeliveryAddressText(Address address) {
    if (address == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    if (address.getCountry() != null) {
      sb.append(address.getCountry()).append(", ");
    }
    if (address.getCity() != null) {
      sb.append(address.getCity()).append(", ");
    }
    if (address.getStreet() != null) {
      sb.append(address.getStreet()).append(" ");
    }
    if (address.getBuilding() != null) {
      sb.append("д. ").append(address.getBuilding()).append(" ");
    }
    if (address.getApartment() != null) {
      sb.append("кв. ").append(address.getApartment()).append(" ");
    }
    if (address.getEntrance() != null) {
      sb.append("подъезд ").append(address.getEntrance()).append(" ");
    }
    if (address.getFloor() != null) {
      sb.append("этаж ").append(address.getFloor()).append(" ");
    }
    if (address.getAddressNote() != null) {
      sb.append("(").append(address.getAddressNote()).append(")");
    }
    String result = sb.toString().trim();
    return result.isEmpty() ? null : result;
  }

  private Address addressFromText(String text) {
    return Address.builder()
        .addressNote(text)
        .build();
  }

  private void saveOrderItems(Long orderId, List<CartItem> items, Connection conn) throws SQLException {
    String sql = "INSERT INTO order_items (order_id, product_name, quantity, price) VALUES (?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (CartItem item : items) {
        stmt.setLong(1, orderId);
        stmt.setString(2, item.getProductName());
        stmt.setInt(3, item.getQuantity());
        stmt.setDouble(4, item.getPrice());
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  private boolean hasDeliveryAddressColumn(Connection conn) throws SQLException {
    if (deliveryAddressColumnPresent != null) {
      return deliveryAddressColumnPresent;
    }

    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, "orders", "delivery_address")) {
      deliveryAddressColumnPresent = rs.next();
    }

    if (!deliveryAddressColumnPresent) {
      logger.warn("delivery_address column is missing in orders table; consider running the latest schema scripts");
    }

    return deliveryAddressColumnPresent;
  }

  private boolean hasPaymentStatusColumn(Connection conn) throws SQLException {
    if (paymentStatusColumnPresent != null) {
      return paymentStatusColumnPresent;
    }

    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, "orders", "payment_status")) {
      paymentStatusColumnPresent = rs.next();
    }

    if (!paymentStatusColumnPresent) {
      logger.warn("payment_status column is missing in orders table; consider running the latest schema scripts");
    }

    return paymentStatusColumnPresent;
  }

  private boolean hasPaymentMethodColumn(Connection conn) throws SQLException {
    if (paymentMethodColumnPresent != null) {
      return paymentMethodColumnPresent;
    }

    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, "orders", "payment_method")) {
      paymentMethodColumnPresent = rs.next();
    }

    if (!paymentMethodColumnPresent) {
      logger.warn("payment_method column is missing in orders table; consider running the latest schema scripts");
    }

    return paymentMethodColumnPresent;
  }

  private boolean hasEstimatedDeliveryTimeColumn(Connection conn) throws SQLException {
    if (estimatedDeliveryTimeColumnPresent != null) {
      return estimatedDeliveryTimeColumnPresent;
    }

    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, "orders", "estimated_delivery_time")) {
      estimatedDeliveryTimeColumnPresent = rs.next();
    }

    if (!estimatedDeliveryTimeColumnPresent) {
      logger.warn("estimated_delivery_time column is missing in orders table; consider running the latest schema scripts");
    }

    return estimatedDeliveryTimeColumnPresent;
  }

}