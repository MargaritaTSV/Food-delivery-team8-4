package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.repository.CourierRepository;
import com.team8.fooddelivery.util.DatabaseConnection;
import com.team8.fooddelivery.util.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CourierServiceImplErrorHandlingTest {

    private CourierServiceImpl courierService;
    private CourierRepository courierRepository;
    private Long testCourierId;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseInitializer.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        courierService = new CourierServiceImpl();
        courierRepository = new CourierRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testCourierId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM couriers WHERE id = ?")) {
                stmt.setLong(1, testCourierId);
                stmt.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("getCourierById: Should return null for non-existent courier")
    void testGetCourierById_NotFound() {
        Courier result = courierService.getCourierById(999999L);
        assertNull(result);
    }

    @Test
    @DisplayName("getOrderHistory: Should return empty list for non-existent courier")
    void testGetOrderHistory_NotFound() {
        var result = courierService.getOrderHistory(999999L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCourierById: Should return courier when exists")
    void testGetCourierById_Success() throws SQLException {
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        testCourierId = courierService.registerNewCourier("Test Courier", uniquePhone, "Password123!", "bike");
        
        Courier result = courierService.getCourierById(testCourierId);
        assertNotNull(result);
        assertEquals(testCourierId, result.getId());
    }

    @Test
    @DisplayName("getOrderHistory: Should return orders when courier exists")
    void testGetOrderHistory_Success() throws SQLException {
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        testCourierId = courierService.registerNewCourier("Test Courier", uniquePhone, "Password123!", "bike");
        
        var result = courierService.getOrderHistory(testCourierId);
        assertNotNull(result);
        // May be empty if no orders assigned
    }
}
