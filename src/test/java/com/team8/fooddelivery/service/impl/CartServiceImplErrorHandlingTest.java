package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.util.DatabaseConnection;
import com.team8.fooddelivery.util.DatabaseInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceImplErrorHandlingTest {

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseInitializer.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        cartService = new CartServiceImpl();
    }

    @Test
    @DisplayName("getCartForClient: Should return null for non-existent client")
    void testGetCartForClient_NotFound() {
        Cart result = cartService.getCartForClient(999999L);
        assertNull(result);
    }

    @Test
    @DisplayName("getCartForClient: Should return cart when client exists")
    void testGetCartForClient_Success() throws SQLException {
        // Create a test client
        com.team8.fooddelivery.service.impl.ClientServiceImpl clientService = 
            new com.team8.fooddelivery.service.impl.ClientServiceImpl(cartService);
        
        com.team8.fooddelivery.model.Address address = com.team8.fooddelivery.model.Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000);
        String uniqueEmail = "cart_test_" + System.currentTimeMillis() + "@test.com";
        
        com.team8.fooddelivery.model.client.Client client = clientService.register(
            uniquePhone, "Password123!", "Cart Test Client", uniqueEmail, address);
        
        // Create cart
        cartService.createCartForClient(client.getId());
        
        Cart result = cartService.getCartForClient(client.getId());
        assertNotNull(result);
        
        // Cleanup
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
            stmt.setLong(1, client.getId());
            stmt.executeUpdate();
        }
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
            stmt.setLong(1, client.getId());
            stmt.executeUpdate();
        }
    }
}
