package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientRepository {
    private static final Logger logger = LoggerFactory.getLogger(ClientRepository.class);
    private final AddressRepository addressRepository = new AddressRepository();

    public Long save(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, phone, password_hash, email, address_id, status, created_at, is_active, order_history) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Long addressId = null;
            if (client.getAddress() != null) {
                addressId = addressRepository.save(client.getAddress());
            }

            stmt.setString(1, client.getName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getPasswordHash());
            stmt.setString(4, client.getEmail());
            stmt.setObject(5, addressId, Types.BIGINT);
            stmt.setString(6, client.getStatus() != null ? client.getStatus().name() : ClientStatus.ACTIVE.name());
            stmt.setTimestamp(7, Timestamp.from(client.getCreatedAt() != null ? client.getCreatedAt() : Instant.now()));
            stmt.setBoolean(8, client.isActive());
            
            Array orderHistoryArray = conn.createArrayOf("text", 
                client.getOrderHistory() != null ? client.getOrderHistory().toArray() : new String[0]);
            stmt.setArray(9, orderHistoryArray);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.debug("Клиент сохранен с id={}", id);
                return id;
            }
            throw new SQLException("Не удалось сохранить клиента");
        }
    }

    public Optional<Client> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs, conn));
            }
            return Optional.empty();
        }
    }

    public Optional<Client> findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM clients WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs, conn));
            }
            return Optional.empty();
        }
    }

    public Optional<Client> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM clients WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs, conn));
            }
            return Optional.empty();
        }
    }

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT * FROM clients";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Client> clients = new ArrayList<>();
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs, conn));
            }
            return clients;
        }
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE clients SET name=?, phone=?, password_hash=?, email=?, address_id=?, " +
                     "status=?, is_active=?, order_history=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Long addressId = null;
            if (client.getAddress() != null) {
                Long existingAddressId = getAddressIdForClient(client.getId(), conn);
                if (existingAddressId != null) {
                    addressRepository.update(existingAddressId, client.getAddress());
                    addressId = existingAddressId;
                } else {
                    addressId = addressRepository.save(client.getAddress());
                }
            }

            stmt.setString(1, client.getName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getPasswordHash());
            stmt.setString(4, client.getEmail());
            stmt.setObject(5, addressId, Types.BIGINT);
            stmt.setString(6, client.getStatus() != null ? client.getStatus().name() : ClientStatus.ACTIVE.name());
            stmt.setBoolean(7, client.isActive());
            
            Array orderHistoryArray = conn.createArrayOf("text", 
                client.getOrderHistory() != null ? client.getOrderHistory().toArray() : new String[0]);
            stmt.setArray(8, orderHistoryArray);
            stmt.setLong(9, client.getId());

            stmt.executeUpdate();
            logger.debug("Клиент обновлен: id={}", client.getId());
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.debug("Клиент удален: id={}", id);
        }
    }

    private Client mapResultSetToClient(ResultSet rs, Connection conn) throws SQLException {
        Client.ClientBuilder builder = Client.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .phone(rs.getString("phone"))
                .passwordHash(rs.getString("password_hash"))
                .email(rs.getString("email"))
                .status(ClientStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .isActive(rs.getBoolean("is_active"));

        Long addressId = rs.getObject("address_id", Long.class);
        if (addressId != null) {
            addressRepository.findById(addressId).ifPresent(builder::address);
        }

        Array orderHistoryArray = rs.getArray("order_history");
        if (orderHistoryArray != null) {
            String[] history = (String[]) orderHistoryArray.getArray();
            List<String> orderHistory = new ArrayList<>();
            for (String entry : history) {
                orderHistory.add(entry);
            }
            builder.orderHistory(orderHistory);
        } else {
            builder.orderHistory(new ArrayList<>());
        }

        return builder.build();
    }

    private Long getAddressIdForClient(Long clientId, Connection conn) throws SQLException {
        String sql = "SELECT address_id FROM clients WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getObject("address_id", Long.class);
            }
        }
        return null;
    }
}

