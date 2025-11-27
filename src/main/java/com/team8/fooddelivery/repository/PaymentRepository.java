package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.client.Payment;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class PaymentRepository {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRepository.class);

    public Long save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, amount, method, status, created_at) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, payment.getOrderId());
            stmt.setObject(2, payment.getAmount());
            stmt.setString(3, payment.getMethod().name());
            stmt.setString(4, payment.getStatus().name());
            stmt.setTimestamp(5, Timestamp.from(payment.getCreatedAt()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.debug("Платеж сохранён: {}", id);
                return id;
            }
            throw new SQLException("Не удалось сохранить платеж");
        }
    }

    public Optional<Payment> findByOrderId(Long orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPayment(rs));
            }
            return Optional.empty();
        }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        return Payment.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .amount(rs.getDouble("amount"))
                .method(PaymentMethodForOrder.valueOf(rs.getString("method")))
                .status(PaymentStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }
}
