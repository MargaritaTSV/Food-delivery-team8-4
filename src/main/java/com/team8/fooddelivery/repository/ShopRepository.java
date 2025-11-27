package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopRepository {
  private static final Logger logger = LoggerFactory.getLogger(ShopRepository.class);
  private final AddressRepository addressRepository = new AddressRepository();
  private final WorkingHoursRepository workingHoursRepository = new WorkingHoursRepository();

  public Long save(Shop shop) throws SQLException {
    String sql = "INSERT INTO shops (naming, description, public_email, email_for_auth, phone_for_auth, public_phone, " +
        "status, address_id, working_hours_id, owner_name, owner_contact_phone, registration_date, rating, type, password) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING shop_id";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      Long addressId = null;
      if (shop.getAddress() != null) {
        addressId = addressRepository.save(shop.getAddress());
      }

      Long workingHoursId = null;
      if (shop.getWorkingHours() != null) {
        workingHoursId = workingHoursRepository.save(shop.getWorkingHours());
      }

      stmt.setString(1, shop.getNaming());
      stmt.setString(2, shop.getDescription());
      stmt.setString(3, shop.getPublicEmail());
      stmt.setString(4, shop.getEmailForAuth());
      stmt.setString(5, shop.getPhoneForAuth());
      stmt.setString(6, shop.getPublicPhone());
      stmt.setString(7, shop.getStatus() != null ? shop.getStatus().name() : ShopStatus.PENDING.name());
      stmt.setObject(8, addressId, Types.BIGINT);
      stmt.setObject(9, workingHoursId, Types.BIGINT);
      stmt.setString(10, shop.getOwnerName());
      stmt.setString(11, shop.getOwnerContactPhone());
      stmt.setTimestamp(12, Timestamp.from(shop.getRegistrationDate() != null ? shop.getRegistrationDate() : Instant.now()));
      stmt.setDouble(13, shop.getRating() != null ? shop.getRating() : 0.0);
      stmt.setString(14, shop.getType() != null ? shop.getType().name() : null);
      stmt.setString(15, shop.getPassword());

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Long id = rs.getLong("shop_id");
        logger.debug("Магазин сохранен с id={}", id);
        return id;
      }
      throw new SQLException("Не удалось сохранить магазин");
    }
  }

  public Optional<Shop> findById(Long shopId) throws SQLException {
    String sql = "SELECT * FROM shops WHERE shop_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, shopId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToShop(rs, conn));
      }
      return Optional.empty();
    }
  }

  public Optional<Shop> findByEmailForAuth(String email) throws SQLException {
    String sql = "SELECT * FROM shops WHERE email_for_auth = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToShop(rs, conn));
      }
      return Optional.empty();
    }
  }

  public Optional<Shop> findByPhoneForAuth(String phone) throws SQLException {
    String sql = "SELECT * FROM shops WHERE phone_for_auth = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, phone);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToShop(rs, conn));
      }
      return Optional.empty();
    }
  }

  public List<Shop> findAll() throws SQLException {
    String sql = "SELECT * FROM shops";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      List<Shop> shops = new ArrayList<>();
      while (rs.next()) {
        shops.add(mapResultSetToShop(rs, conn));
      }
      return shops;
    }
  }

  public void update(Shop shop) throws SQLException {
    String sql = "UPDATE shops SET naming=?, description=?, public_email=?, email_for_auth=?, phone_for_auth=?, " +
        "public_phone=?, status=?, address_id=?, working_hours_id=?, owner_name=?, owner_contact_phone=?, " +
        "rating=?, type=?, password=? WHERE shop_id=?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      Long addressId = null;
      if (shop.getAddress() != null) {
        Long existingAddressId = getAddressIdForShop(shop.getShopId(), conn);
        if (existingAddressId != null) {
          addressRepository.update(existingAddressId, shop.getAddress());
          addressId = existingAddressId;
        } else {
          addressId = addressRepository.save(shop.getAddress());
        }
      }

      Long workingHoursId = null;
      if (shop.getWorkingHours() != null) {
        Long existingWorkingHoursId = getWorkingHoursIdForShop(shop.getShopId(), conn);
        if (existingWorkingHoursId != null) {
          workingHoursRepository.update(existingWorkingHoursId, shop.getWorkingHours());
          workingHoursId = existingWorkingHoursId;
        } else {
          workingHoursId = workingHoursRepository.save(shop.getWorkingHours());
        }
      }

      stmt.setString(1, shop.getNaming());
      stmt.setString(2, shop.getDescription());
      stmt.setString(3, shop.getPublicEmail());
      stmt.setString(4, shop.getEmailForAuth());
      stmt.setString(5, shop.getPhoneForAuth());
      stmt.setString(6, shop.getPublicPhone());
      stmt.setString(7, shop.getStatus() != null ? shop.getStatus().name() : ShopStatus.PENDING.name());
      stmt.setObject(8, addressId, Types.BIGINT);
      stmt.setObject(9, workingHoursId, Types.BIGINT);
      stmt.setString(10, shop.getOwnerName());
      stmt.setString(11, shop.getOwnerContactPhone());
      stmt.setDouble(12, shop.getRating() != null ? shop.getRating() : 0.0);
      stmt.setString(13, shop.getType() != null ? shop.getType().name() : null);
      stmt.setString(14, shop.getPassword());
      stmt.setLong(15, shop.getShopId());

      stmt.executeUpdate();
      logger.debug("Магазин обновлен: id={}", shop.getShopId());
    }
  }

  public void delete(Long shopId) throws SQLException {
    String sql = "DELETE FROM shops WHERE shop_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, shopId);
      stmt.executeUpdate();
      logger.debug("Магазин удален: id={}", shopId);
    }
  }

  private Shop mapResultSetToShop(ResultSet rs, Connection conn) throws SQLException {
    Shop shop = new Shop();
    shop.setShopId(rs.getLong("shop_id"));
    shop.setNaming(rs.getString("naming"));
    shop.setDescription(rs.getString("description"));
    shop.setPublicEmail(rs.getString("public_email"));
    shop.setEmailForAuth(rs.getString("email_for_auth"));
    shop.setPhoneForAuth(rs.getString("phone_for_auth"));
    shop.setPublicPhone(rs.getString("public_phone"));
    shop.setStatus(ShopStatus.valueOf(rs.getString("status")));
    shop.setOwnerName(rs.getString("owner_name"));
    shop.setOwnerContactPhone(rs.getString("owner_contact_phone"));
    // registrationDate - final поле, инициализируется автоматически
    shop.setRating(rs.getDouble("rating"));
    shop.setPassword(rs.getString("password"));

    String typeStr = rs.getString("type");
    if (typeStr != null) {
      shop.setType(ShopType.valueOf(typeStr));
    }

    Long addressId = rs.getObject("address_id", Long.class);
    if (addressId != null) {
      addressRepository.findById(addressId).ifPresent(shop::setAddress);
    }

    Long workingHoursId = rs.getObject("working_hours_id", Long.class);
    if (workingHoursId != null) {
      workingHoursRepository.findById(workingHoursId).ifPresent(shop::setWorkingHours);
    }

    return shop;
  }

  private Long getAddressIdForShop(Long shopId, Connection conn) throws SQLException {
    String sql = "SELECT address_id FROM shops WHERE shop_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, shopId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getObject("address_id", Long.class);
      }
    }
    return null;
  }

  private Long getWorkingHoursIdForShop(Long shopId, Connection conn) throws SQLException {
    String sql = "SELECT working_hours_id FROM shops WHERE shop_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, shopId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getObject("working_hours_id", Long.class);
      }
    }
    return null;
  }
}