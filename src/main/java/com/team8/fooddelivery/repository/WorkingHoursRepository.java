package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class WorkingHoursRepository {
  private static final Logger logger = LoggerFactory.getLogger(WorkingHoursRepository.class);

  public Long save(WorkingHours workingHours) throws SQLException {
    String sql = "INSERT INTO working_hours (monday, tuesday, wednesday, thursday, friday, saturday, sunday) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, workingHours.getMonday());
      stmt.setString(2, workingHours.getTuesday());
      stmt.setString(3, workingHours.getWednesday());
      stmt.setString(4, workingHours.getThursday());
      stmt.setString(5, workingHours.getFriday());
      stmt.setString(6, workingHours.getSaturday());
      stmt.setString(7, workingHours.getSunday());

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Long id = rs.getLong("id");
        logger.debug("Рабочие часы сохранены с id={}", id);
        return id;
      }
      throw new SQLException("Не удалось сохранить рабочие часы");
    }
  }

  public Optional<WorkingHours> findById(Long id) throws SQLException {
    String sql = "SELECT * FROM working_hours WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapResultSetToWorkingHours(rs));
      }
      return Optional.empty();
    }
  }

  public void update(Long id, WorkingHours workingHours) throws SQLException {
    String sql = "UPDATE working_hours SET monday=?, tuesday=?, wednesday=?, thursday=?, friday=?, saturday=?, sunday=? WHERE id=?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, workingHours.getMonday());
      stmt.setString(2, workingHours.getTuesday());
      stmt.setString(3, workingHours.getWednesday());
      stmt.setString(4, workingHours.getThursday());
      stmt.setString(5, workingHours.getFriday());
      stmt.setString(6, workingHours.getSaturday());
      stmt.setString(7, workingHours.getSunday());
      stmt.setLong(8, id);

      stmt.executeUpdate();
      logger.debug("Рабочие часы обновлены: id={}", id);
    }
  }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM working_hours WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
            logger.debug("Рабочие часы удалены: id={}", id);
        }
    }

  private WorkingHours mapResultSetToWorkingHours(ResultSet rs) throws SQLException {
    return new WorkingHours(
        rs.getString("monday"),
        rs.getString("tuesday"),
        rs.getString("wednesday"),
        rs.getString("thursday"),
        rs.getString("friday"),
        rs.getString("saturday"),
        rs.getString("sunday")
    );
  }
}
