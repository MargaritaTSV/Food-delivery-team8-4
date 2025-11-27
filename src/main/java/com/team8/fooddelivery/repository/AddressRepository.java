package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class AddressRepository {
    private static final Logger logger = LoggerFactory.getLogger(AddressRepository.class);

    public Long save(Address address) throws SQLException {
        String sql = "INSERT INTO addresses (country, city, street, building, apartment, entrance, floor, " +
                     "latitude, longitude, address_note, district) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, address.getCountry());
            stmt.setString(2, address.getCity());
            stmt.setString(3, address.getStreet());
            stmt.setString(4, address.getBuilding());
            stmt.setString(5, address.getApartment());
            stmt.setString(6, address.getEntrance());
            stmt.setObject(7, address.getFloor(), Types.INTEGER);
            stmt.setObject(8, address.getLatitude(), Types.DOUBLE);
            stmt.setObject(9, address.getLongitude(), Types.DOUBLE);
            stmt.setString(10, address.getAddressNote());
            stmt.setString(11, address.getDistrict());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.debug("Адрес сохранен с id={}", id);
                return id;
            }
            throw new SQLException("Не удалось сохранить адрес");
        }
    }

    public Optional<Address> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAddress(rs));
            }
            return Optional.empty();
        }
    }

    public void update(Long id, Address address) throws SQLException {
        String sql = "UPDATE addresses SET country=?, city=?, street=?, building=?, apartment=?, " +
                     "entrance=?, floor=?, latitude=?, longitude=?, address_note=?, district=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, address.getCountry());
            stmt.setString(2, address.getCity());
            stmt.setString(3, address.getStreet());
            stmt.setString(4, address.getBuilding());
            stmt.setString(5, address.getApartment());
            stmt.setString(6, address.getEntrance());
            stmt.setObject(7, address.getFloor(), Types.INTEGER);
            stmt.setObject(8, address.getLatitude(), Types.DOUBLE);
            stmt.setObject(9, address.getLongitude(), Types.DOUBLE);
            stmt.setString(10, address.getAddressNote());
            stmt.setString(11, address.getDistrict());
            stmt.setLong(12, id);

            int rows = stmt.executeUpdate();
            logger.debug("Адрес обновлен: id={}, rows={}", id, rows);
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM addresses WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.debug("Адрес удален: id={}", id);
        }
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        return Address.builder()
                .country(rs.getString("country"))
                .city(rs.getString("city"))
                .street(rs.getString("street"))
                .building(rs.getString("building"))
                .apartment(rs.getString("apartment"))
                .entrance(rs.getString("entrance"))
                .floor(rs.getObject("floor", Integer.class))
                .latitude(rs.getObject("latitude", Double.class))
                .longitude(rs.getObject("longitude", Double.class))
                .addressNote(rs.getString("address_note"))
                .district(rs.getString("district"))
                .build();
    }
}

