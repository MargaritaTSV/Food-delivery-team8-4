package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.client.Client;
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

class ClientServiceImplErrorHandlingTest {

    private ClientServiceImpl clientService;
    private CartServiceImpl cartService;
    private Long testClientId;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseInitializer.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        cartService = new CartServiceImpl();
        clientService = new ClientServiceImpl(cartService);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testClientId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("getByPhone: Should return null for non-existent phone")
    void testGetByPhone_NotFound() {
        // Use a phone number that definitely doesn't exist
        String nonExistentPhone = "+7999" + (System.currentTimeMillis() % 10000000 + 9999999);
        Client result = clientService.getByPhone(nonExistentPhone);
        assertNull(result);
    }

    @Test
    @DisplayName("getByEmail: Should return null for non-existent email")
    void testGetByEmail_NotFound() {
        Client result = clientService.getByEmail("nonexistent@example.com");
        assertNull(result);
    }

    @Test
    @DisplayName("getByPhone: Should return client when exists")
    void testGetByPhone_Success() throws SQLException {
        com.team8.fooddelivery.model.Address address = com.team8.fooddelivery.model.Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        String uniqueEmail = "phone_test_" + System.currentTimeMillis() + "@test.com";
        
        Client client = clientService.register(uniquePhone, "Password123!", "Phone Test Client", uniqueEmail, address);
        testClientId = client.getId();
        
        Client result = clientService.getByPhone(uniquePhone);
        assertNotNull(result);
        assertEquals(uniquePhone, result.getPhone());
    }

    @Test
    @DisplayName("getByEmail: Should return client when exists")
    void testGetByEmail_Success() throws SQLException {
        com.team8.fooddelivery.model.Address address = com.team8.fooddelivery.model.Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        String uniqueEmail = "email_test_" + System.currentTimeMillis() + "@test.com";
        
        Client client = clientService.register(uniquePhone, "Password123!", "Email Test Client", uniqueEmail, address);
        testClientId = client.getId();
        
        Client result = clientService.getByEmail(uniqueEmail);
        assertNotNull(result);
        assertEquals(uniqueEmail, result.getEmail());
    }

    @Test
    @DisplayName("listAll: Should return list of clients")
    void testListAll() {
        var result = clientService.listAll();
        assertNotNull(result);
        // May be empty if no clients exist
    }

    @Test
    @DisplayName("getOrderHistory: Should return empty list for non-existent client")
    void testGetOrderHistory_NotFound() {
        var result = clientService.getOrderHistory(999999L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getById: Should return null for non-existent client")
    void testGetById_NotFound() {
        Client result = clientService.getById(999999L);
        assertNull(result);
    }
}
