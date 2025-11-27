package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
  private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

  public Long save(Product product) throws SQLException {
    String sql = "INSERT INTO products (shop_id, name, description, weight, price, category, is_available, cooking_time_minutes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING product_id";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setObject(1, product.getProductId() != null ? null : null, Types.BIGINT); // shop_id будет установлен отдельно
      stmt.setString(2, product.getName());
      stmt.setString(3, product.getDescription());
      stmt.setObject(4, product.getWeight(), Types.DOUBLE);
      stmt.setDouble(5, product.getPrice());
      stmt.setString(6, product.getCategory() != null ? product.getCategory().name() : null);
      stmt.setBoolean(7, product.getAvailable());
      stmt.setLong(8, product.getCookingTimeMinutes() != null ? product.getCookingTimeMinutes().getSeconds() : 0);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Long id = rs.getLong("product_id");
        logger.debug("Продукт сохранен с id={}", id);
        return id;
      }
      throw new SQLException("Не удалось сохранить продукт");
    }
  }

  public Long saveForShop(Long shopId, Product product) throws SQLException {
    String sql = "INSERT INTO products (shop_id, name, description, weight, price, category, is_available, cooking_time_minutes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING product_id";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, shopId);
      stmt.setString(2, product.getName());
      stmt.setString(3, product.getDescription());
      stmt.setObject(4, product.getWeight(), Types.DOUBLE);
      stmt.setDouble(5, product.getPrice());
      stmt.setString(6, product.getCategory() != null ? product.getCategory().name() : null);
      stmt.setBoolean(7, product.getAvailable());
      stmt.setLong(8, product.getCookingTimeMinutes() != null ? product.getCookingTimeMinutes().getSeconds() : 0);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Long id = rs.getLong("product_id");
        logger.debug("Продукт сохранен с id={} для магазина shopId={}", id, shopId);
        return id;
      }
      throw new SQLException("Не удалось сохранить продукт");
    }
  }

  public Optional<Product> findById(Long productId) throws SQLException {
    String sql = "SELECT * FROM products WHERE product_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, productId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToProduct(rs));
      }
      return Optional.empty();
    }
  }

  public List<Product> findByShopId(Long shopId) throws SQLException {
    String sql = "SELECT * FROM products WHERE shop_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, shopId);
      ResultSet rs = stmt.executeQuery();

      List<Product> products = new ArrayList<>();
      while (rs.next()) {
        products.add(mapResultSetToProduct(rs));
      }
      return products;
    }
  }

  public List<Product> findByShopIdAndCategory(Long shopId, ProductCategory category) throws SQLException {
    String sql = "SELECT * FROM products WHERE shop_id = ? AND category = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, shopId);
      stmt.setString(2, category.name());
      ResultSet rs = stmt.executeQuery();

      List<Product> products = new ArrayList<>();
      while (rs.next()) {
        products.add(mapResultSetToProduct(rs));
      }
      return products;
    }
  }

  public void update(Product product) throws SQLException {
    String sql = "UPDATE products SET name=?, description=?, weight=?, price=?, category=?, is_available=?, cooking_time_minutes=? WHERE product_id=?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, product.getName());
      stmt.setString(2, product.getDescription());
      stmt.setObject(3, product.getWeight(), Types.DOUBLE);
      stmt.setDouble(4, product.getPrice());
      stmt.setString(5, product.getCategory() != null ? product.getCategory().name() : null);
      stmt.setBoolean(6, product.getAvailable());
      stmt.setLong(7, product.getCookingTimeMinutes() != null ? product.getCookingTimeMinutes().getSeconds() : 0);
      stmt.setLong(8, product.getProductId());

      stmt.executeUpdate();
      logger.debug("Продукт обновлен: id={}", product.getProductId());
    }
  }

  public void delete(Long productId) throws SQLException {
    String sql = "DELETE FROM products WHERE product_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, productId);
      stmt.executeUpdate();
      logger.debug("Продукт удален: id={}", productId);
    }
  }

  private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
    Long productId = rs.getLong("product_id");
    String name = rs.getString("name");
    String description = rs.getString("description");
    Double weight = rs.getObject("weight", Double.class);
    Double price = rs.getDouble("price");

    ProductCategory category = null;
    String categoryStr = rs.getString("category");
    if (categoryStr != null) {
      category = ProductCategory.valueOf(categoryStr);
    }

    boolean isAvailable = rs.getBoolean("is_available");

    Long cookingTimeSeconds = rs.getLong("cooking_time_minutes");
    Duration cookingTimeMinutes = Duration.ofSeconds(cookingTimeSeconds);

    return new Product(productId, name, description, weight, price, category, isAvailable, cookingTimeMinutes);
  }
}