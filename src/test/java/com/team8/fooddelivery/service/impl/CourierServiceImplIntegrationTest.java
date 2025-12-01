package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourierServiceImplIntegrationTest {

    private CourierServiceImpl courierService;
    private static Long testCourierId;
    private static String testPhone;

    @BeforeAll
    static void setupDatabase() {
        DatabaseConnection.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
    }

    @AfterAll
    static void cleanup() {
        if (testCourierId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM couriers WHERE id = ?")) {
                stmt.setLong(1, testCourierId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    @BeforeEach
    void setUp() {
        courierService = new CourierServiceImpl();
        testPhone = "+7999" + (System.currentTimeMillis() % 10000000);
    }

    @Test
    @Order(1)
    @DisplayName("Register new courier")
    void testRegisterNewCourier() {
        Long id = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        assertNotNull(id);
        assertTrue(id > 0);
        testCourierId = id;
    }

    @Test
    @Order(2)
    @DisplayName("Login courier")
    void testLogin() {
        // Убеждаемся, что курьер зарегистрирован
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        // Получаем курьера по ID, чтобы убедиться, что он существует
        Courier registeredCourier = courierService.getCourierById(testCourierId);
        if (registeredCourier != null) {
            testPhone = registeredCourier.getPhoneNumber(); // Используем телефон из зарегистрированного курьера
        }
        // Убеждаемся, что курьер активен (не обязательно для логина)
        try {
            courierService.startShift(testCourierId);
        } catch (Exception e) {
            // Игнорируем ошибки - курьер может быть уже на смене
        }
        Courier courier = courierService.login(testPhone, "Password123!");
        assertNotNull(courier, "Courier should not be null after login with phone: " + testPhone);
        assertEquals(testPhone, courier.getPhoneNumber());
    }

    @Test
    @Order(3)
    @DisplayName("Login with wrong password")
    void testLoginWrongPassword() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        Courier courier = courierService.login(testPhone, "WrongPassword");
        assertNull(courier);
    }

    @Test
    @Order(4)
    @DisplayName("Get courier by ID")
    void testGetCourierById() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        Courier courier = courierService.getCourierById(testCourierId);
        assertNotNull(courier);
        assertEquals(testCourierId, courier.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Start shift")
    void testStartShift() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        courierService.startShift(testCourierId);
        Courier courier = courierService.getCourierById(testCourierId);
        assertEquals(CourierStatus.ON_SHIFT, courier.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("End shift")
    void testEndShift() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        courierService.endShift(testCourierId);
        Courier courier = courierService.getCourierById(testCourierId);
        assertEquals(CourierStatus.OFF_SHIFT, courier.getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("Get order history")
    void testGetOrderHistory() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        var history = courierService.getOrderHistory(testCourierId);
        assertNotNull(history);
    }

    @Test
    @Order(8)
    @DisplayName("Update courier data")
    void testUpdateCourierData() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        assertDoesNotThrow(() -> {
            courierService.updateCourierData(testCourierId, "Updated Name", testPhone, "NewPass123!", "car");
        });
        Courier courier = courierService.getCourierById(testCourierId);
        assertEquals("Updated Name", courier.getName());
        assertEquals("car", courier.getTransportType());
    }

    @Test
    @Order(9)
    @DisplayName("Accept order")
    void testAcceptOrder() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        // Create test order first
        // This test requires order setup, so we'll just test the method exists
        courierService.startShift(testCourierId);
        // Accept order would need a real order, so we test that method can be called
        boolean result = courierService.acceptOrder(testCourierId, -1L);
        assertFalse(result); // Should return false for non-existent order
    }

    @Test
    @Order(10)
    @DisplayName("Pickup order")
    void testPickupOrder() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        assertDoesNotThrow(() -> {
            courierService.pickupOrder(testCourierId, -1L);
        });
    }

    @Test
    @Order(11)
    @DisplayName("Complete order")
    void testCompleteOrder() {
        if (testCourierId == null) {
            testCourierId = courierService.registerNewCourier("Test Courier", testPhone, "Password123!", "bike");
        }
        assertDoesNotThrow(() -> {
            courierService.completeOrder(testCourierId, -1L);
        });
    }
}

