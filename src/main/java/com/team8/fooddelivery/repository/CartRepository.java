package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository {
    private static final Logger logger = LoggerFactory.getLogger(CartRepository.class);

    public Long save(Cart cart) throws SQLException {
        String sql = "INSERT INTO carts (client_id) VALUES (?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cart.getClientId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.debug("Корзина сохранена с id={}", id);
                return id;
            }
            throw new SQLException("Не удалось сохранить корзину");
        }
    }

    public Optional<Cart> findByClientId(Long clientId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE client_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cart cart = new Cart();
                cart.setId(rs.getLong("id"));
                cart.setClientId(rs.getLong("client_id"));
                
                // Загружаем элементы корзины
                List<CartItem> items = findCartItemsByCartId(cart.getId(), conn);
                cart.setItems(items);
                
                return Optional.of(cart);
            }
            return Optional.empty();
        }
    }

    public Optional<Cart> findById(Long cartId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cart cart = new Cart();
                cart.setId(rs.getLong("id"));
                cart.setClientId(rs.getLong("client_id"));
                
                // Загружаем элементы корзины
                List<CartItem> items = findCartItemsByCartId(cart.getId(), conn);
                cart.setItems(items);
                
                return Optional.of(cart);
            }
            return Optional.empty();
        }
    }

    public void delete(Long cartId) throws SQLException {
        // Удаление элементов корзины происходит автоматически через CASCADE
        String sql = "DELETE FROM carts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.executeUpdate();
            logger.debug("Корзина удалена: id={}", cartId);
        }
    }

    // Методы для работы с элементами корзины
    public Long saveCartItem(CartItem item) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_id, product_id, product_name, quantity, price) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, item.getCartId());
            stmt.setLong(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setInt(4, item.getQuantity());
            stmt.setDouble(5, item.getPrice());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.debug("Элемент корзины сохранен с id={}", id);
                return id;
            }
            throw new SQLException("Не удалось сохранить элемент корзины");
        }
    }

    public List<CartItem> findCartItemsByCartId(Long cartId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            ResultSet rs = stmt.executeQuery();

            List<CartItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapResultSetToCartItem(rs));
            }
            return items;
        }
    }

    public List<CartItem> findCartItemsByCartId(Long cartId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findCartItemsByCartId(cartId, conn);
        }
    }

    public void updateCartItem(CartItem item) throws SQLException {
        String sql = "UPDATE cart_items SET quantity=?, price=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getQuantity());
            stmt.setDouble(2, item.getPrice());
            stmt.setLong(3, item.getId());

            stmt.executeUpdate();
            logger.debug("Элемент корзины обновлен: id={}", item.getId());
        }
    }

    public void deleteCartItem(Long itemId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, itemId);
            stmt.executeUpdate();
            logger.debug("Элемент корзины удален: id={}", itemId);
        }
    }

    public void deleteCartItemByProductId(Long cartId, Long productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
            logger.debug("Элемент корзины удален: cartId={}, productId={}", cartId, productId);
        }
    }

    public void clearCart(Long cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, cartId);
            stmt.executeUpdate();
            logger.debug("Корзина очищена: cartId={}", cartId);
        }
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        return CartItem.builder()
                .id(rs.getLong("id"))
                .cartId(rs.getLong("cart_id"))
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .quantity(rs.getInt("quantity"))
                .price(rs.getDouble("price"))
                .build();
    }
}

