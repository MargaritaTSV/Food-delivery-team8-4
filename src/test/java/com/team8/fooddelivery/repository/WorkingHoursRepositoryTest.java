package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkingHoursRepositoryTest {

  private WorkingHoursRepository workingHoursRepository;
  private static Long testHoursId;

  @BeforeEach
  public void setUp() {
    workingHoursRepository = new WorkingHoursRepository();
  }

  @AfterAll
  public static void cleanUp() {
    if (testHoursId != null) {
      try (Connection conn = DatabaseConnection.getConnection();
          var stmt = conn.prepareStatement("DELETE FROM working_hours WHERE id = ?")) {
        stmt.setLong(1, testHoursId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        System.err.println("Ошибка при очистке: " + e.getMessage());
      }
    }
  }

  // --- CRUD Tests ---

  @Test
  @Order(1)
  @DisplayName("1. Сохранение и поиск часов работы")
  public void saveAndFindById_shouldWork() throws SQLException {
    WorkingHours initialHours = new WorkingHours("9", "9", "9", "9", "9", "10", "CLOSED");
    Long id = workingHoursRepository.save(initialHours);
    testHoursId = id;

    assertNotNull(id);

    Optional<WorkingHours> foundHoursOpt = workingHoursRepository.findById(id);
    assertTrue(foundHoursOpt.isPresent());
    assertEquals("CLOSED", foundHoursOpt.get().getSunday());
  }

  @Test
  @Order(2)
  @DisplayName("2. Обновление часов работы")
  public void update_shouldModifyDetails() throws SQLException {
    WorkingHours hoursToUpdate = new WorkingHours("8", "8", "8", "8", "8", "10", "18");

    // Обновляем существующую запись, созданную в тесте 1
    workingHoursRepository.update(testHoursId, hoursToUpdate);

    Optional<WorkingHours> updatedHoursOpt = workingHoursRepository.findById(testHoursId);
    assertTrue(updatedHoursOpt.isPresent());
    assertEquals("8", updatedHoursOpt.get().getMonday(), "Понедельник должен быть обновлен");
    assertEquals("18", updatedHoursOpt.get().getSunday(), "Воскресенье должно быть обновлено");
  }

  @Test
  @Order(3)
  @DisplayName("3. Удаление часов работы")
  public void delete_shouldRemoveWorkingHours() throws SQLException {
    // Создаем новую запись, чтобы протестировать удаление
    WorkingHours tempHours = new WorkingHours("9", "9", "9", "9", "9", "10", "CLOSED");
    Long tempId = workingHoursRepository.save(tempHours);

    workingHoursRepository.delete(tempId);

    Optional<WorkingHours> result = workingHoursRepository.findById(tempId);
    assertFalse(result.isPresent(), "Часы работы не должны быть найдены после удаления");
  }

  // --- Edge Cases for Coverage ---

  @Test
  @Order(4)
  @DisplayName("4. findById должен вернуть Optional.empty() для несуществующего ID")
  public void findById_shouldReturnEmpty_whenNotFound() throws SQLException {
    Optional<WorkingHours> result = workingHoursRepository.findById(-1L);
    assertFalse(result.isPresent());
  }
}