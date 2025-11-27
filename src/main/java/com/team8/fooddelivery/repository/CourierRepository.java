package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourierRepository {
  private static final Logger logger = LoggerFactory.getLogger(CourierRepository.class);

  public Long save(Courier courier) throws SQLException {
    String sql = "INSERT INTO couriers (name, password, phone_number, status, transport_type, current_order_id, current_balance, bank_card) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, courier.getName());
      stmt.setString(2, courier.getPassword());
      stmt.setString(3, courier.getPhoneNumber());
      stmt.setString(4, courier.getStatus() != null ? courier.getStatus().name() : CourierStatus.OFF_SHIFT.name());
      stmt.setString(5, courier.getTransportType());
      stmt.setObject(6, courier.getCurrentOrderId(), Types.BIGINT);
      stmt.setDouble(7, courier.getCurrentBalance() != null ? courier.getCurrentBalance() : 0.0);
      stmt.setObject(8, courier.getBankCard(), Types.BIGINT);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Long id = rs.getLong("id");
        logger.debug("Курьер сохранен с id={}", id);
        return id;
      }
      throw new SQLException("Не удалось сохранить курьера");
    }
  }

  public Optional<Courier> findById(Long id) throws SQLException {
    String sql = "SELECT * FROM couriers WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToCourier(rs));
      }
      return Optional.empty();
    }
  }

  public Optional<Courier> findByPhoneNumber(String phoneNumber) throws SQLException {
    String sql = "SELECT * FROM couriers WHERE phone_number = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, phoneNumber);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToCourier(rs));
      }
      return Optional.empty();
    }
  }

  public List<Courier> findAll() throws SQLException {
    String sql = "SELECT * FROM couriers";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      List<Courier> couriers = new ArrayList<>();
      while (rs.next()) {
        couriers.add(mapResultSetToCourier(rs));
      }
      return couriers;
    }
  }

  public void update(Courier courier) throws SQLException {
    String sql = "UPDATE couriers SET name=?, password=?, phone_number=?, status=?, transport_type=?, " +
        "current_order_id=?, current_balance=?, bank_card=? WHERE id=?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, courier.getName());
      stmt.setString(2, courier.getPassword());
      stmt.setString(3, courier.getPhoneNumber());
      stmt.setString(4, courier.getStatus() != null ? courier.getStatus().name() : CourierStatus.OFF_SHIFT.name());
      stmt.setString(5, courier.getTransportType());
      stmt.setObject(6, courier.getCurrentOrderId(), Types.BIGINT);
      stmt.setDouble(7, courier.getCurrentBalance() != null ? courier.getCurrentBalance() : 0.0);
      stmt.setObject(8, courier.getBankCard(), Types.BIGINT);
      stmt.setLong(9, courier.getId());

      stmt.executeUpdate();
      logger.debug("Курьер обновлен: id={}", courier.getId());
    }
  }

  public void delete(Long id) throws SQLException {
    String sql = "DELETE FROM couriers WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      stmt.executeUpdate();
      logger.debug("Курьер удален: id={}", id);
    }
  }

  private Courier mapResultSetToCourier(ResultSet rs) throws SQLException {
    Courier courier = new Courier();
    courier.setId(rs.getLong("id"));
    courier.setName(rs.getString("name"));
    courier.setPassword(rs.getString("password"));
    courier.setPhoneNumber(rs.getString("phone_number"));
    courier.setStatus(CourierStatus.valueOf(rs.getString("status")));
    courier.setTransportType(rs.getString("transport_type"));
    courier.setCurrentOrderId(rs.getObject("current_order_id", Long.class));
    courier.setCurrentBalance(rs.getDouble("current_balance"));
    courier.setBankCard(rs.getObject("bank_card", Long.class));
    return courier;
  }
}

