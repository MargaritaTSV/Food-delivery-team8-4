package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.AuthResponse;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientServiceImplIntegrationTest {

    private ClientServiceImpl clientService;
    private CartServiceImpl cartService;
    private static Long testClientId;
    private static String testPhone;
    private static String testEmail;

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
        if (testClientId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM carts WHERE client_id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
                stmt.setLong(1, testClientId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl();
        clientService = new ClientServiceImpl(cartService);
        // Формат телефона: +7 или 8 + 10 цифр
        // Генерируем уникальные значения только если testClientId еще не установлен
        if (testClientId == null) {
            long suffix = System.currentTimeMillis() % 10000000000L;
            testPhone = "+7" + String.format("%010d", suffix).substring(0, 10);
            testEmail = "test_" + System.currentTimeMillis() + "@test.com";
        }
    }

    @Test
    @Order(1)
    @DisplayName("Register client with valid data")
    void testRegister() {
        Address address = Address.builder()
                .country("Russia")
                .city("Moscow")
                .street("Test Street")
                .building("1")
                .apartment("10")
                .entrance("1")
                .floor(1)
                .latitude(55.7558)
                .longitude(37.6173)
                .build();

        Client client = clientService.register(testPhone, "Password123!", "Test User", testEmail, address);
        assertNotNull(client);
        assertNotNull(client.getId());
        testClientId = client.getId();
        // Обновляем testPhone и testEmail из зарегистрированного клиента
        testPhone = client.getPhone();
        testEmail = client.getEmail();
        assertEquals(ClientStatus.ACTIVE, client.getStatus());
        assertTrue(client.isActive());
    }

    @Test
    @Order(2)
    @DisplayName("Register with invalid phone")
    void testRegisterInvalidPhone() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("invalid", "Password123!", "Test", testEmail + "2", address);
        });
    }

    @Test
    @Order(3)
    @DisplayName("Register with invalid email")
    void testRegisterInvalidEmail() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("+79991234568", "Password123!", "Test", "invalid-email", address);
        });
    }

    @Test
    @Order(4)
    @DisplayName("Register with weak password")
    void testRegisterWeakPassword() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("+79991234569", "weak", "Test", testEmail + "3", address);
        });
    }

    @Test
    @Order(5)
    @DisplayName("Register with invalid address")
    void testRegisterInvalidAddress() {
        Address invalidAddress = Address.builder()
                .country("").city("").street("").building("").build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("+79991234570", "Password123!", "Test", testEmail + "4", invalidAddress);
        });
    }

    @Test
    @Order(6)
    @DisplayName("Register with null address")
    void testRegisterNullAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("+79991234571", "Password123!", "Test", testEmail + "5", null);
        });
    }

    @Test
    @Order(5)
    @DisplayName("Get client by ID")
    void testGetById() {
        if (testClientId == null) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            Client client = clientService.register(phone, "Password123!", "Test", testEmail + "4", address);
            testClientId = client.getId();
        }
        Client found = clientService.getById(testClientId);
        assertNotNull(found);
        assertEquals(testClientId, found.getId());
    }

    @Test
    @Order(6)
    @DisplayName("Get client by phone")
    void testGetByPhone() {
        if (testClientId == null) return;
        Client found = clientService.getByPhone(testPhone);
        assertNotNull(found);
        assertEquals(testPhone, found.getPhone());
    }

    @Test
    @Order(7)
    @DisplayName("Deactivate client")
    void testDeactivate() {
        if (testClientId == null) return;
        boolean result = clientService.deactivate(testClientId);
        assertTrue(result);
        Client client = clientService.getById(testClientId);
        assertFalse(client.isActive());
        assertEquals(ClientStatus.INACTIVE, client.getStatus());
    }

    @Test
    @Order(8)
    @DisplayName("Activate client")
    void testActivate() {
        if (testClientId == null) return;
        boolean result = clientService.activate(testClientId);
        assertTrue(result);
        Client client = clientService.getById(testClientId);
        assertTrue(client.isActive());
        assertEquals(ClientStatus.ACTIVE, client.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("List all clients")
    void testListAll() {
        var clients = clientService.listAll();
        assertNotNull(clients);
        assertFalse(clients.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Get order history")
    void testGetOrderHistory() {
        if (testClientId == null) return;
        var history = clientService.getOrderHistory(testClientId);
        assertNotNull(history);
    }

    @Test
    @Order(11)
    @DisplayName("Add order history entry")
    void testAddOrderHistoryEntry() {
        if (testClientId == null) return;
        clientService.addOrderHistoryEntry(testClientId, "Test order entry");
        var history = clientService.getOrderHistory(testClientId);
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(h -> h.contains("Test order entry")));
    }

    @Test
    @Order(12)
    @DisplayName("Authenticate with valid credentials")
    void testAuthenticate() {
        if (testClientId == null) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            Client client = clientService.register(phone, "Password123!", "Test", testEmail + "auth", address);
            testClientId = client.getId();
            testPhone = client.getPhone(); // Используем телефон из зарегистрированного клиента
        }
        boolean result = clientService.authenticate(testPhone, "Password123!");
        assertTrue(result, "Authentication should succeed for phone: " + testPhone);
    }

    @Test
    @Order(13)
    @DisplayName("Authenticate with invalid password")
    void testAuthenticateInvalidPassword() {
        if (testClientId == null) return;
        boolean result = clientService.authenticate(testPhone, "WrongPassword");
        assertFalse(result);
    }

    @Test
    @Order(14)
    @DisplayName("Login with valid credentials")
    void testLogin() {
        if (testClientId == null) {
            // Если клиент не зарегистрирован, регистрируем его
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            Client client = clientService.register(phone, "Password123!", "Test", testEmail + "login", address);
            testClientId = client.getId();
            testPhone = client.getPhone(); // Используем телефон из зарегистрированного клиента
        }
        AuthResponse response = clientService.login(testPhone, "Password123!");
        assertNotNull(response, "Login should succeed for phone: " + testPhone);
        assertNotNull(response.getAuthToken());
        assertEquals(testClientId, response.getClientId());
    }

    @Test
    @Order(15)
    @DisplayName("Login with invalid credentials")
    void testLoginInvalid() {
        if (testClientId == null) return;
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.login(testPhone, "WrongPassword");
        });
    }

    @Test
    @Order(16)
    @DisplayName("Update client")
    void testUpdate() {
        if (testClientId == null) return;
        Address newAddress = Address.builder()
                .country("Russia").city("SPB").street("New Street").building("2")
                .apartment("20").entrance("2").floor(2)
                .latitude(59.9311).longitude(30.3609).build();
        Client updated = clientService.update(testClientId, "Updated Name", testEmail + "updated", newAddress);
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    @Order(17)
    @DisplayName("Change password")
    void testChangePassword() {
        if (testClientId == null) return;
        boolean result = clientService.changePassword(testClientId, "Password123!", "NewPass123!");
        assertTrue(result);
    }

    @Test
    @Order(18)
    @DisplayName("Change password with wrong old password")
    void testChangePasswordWrongOld() {
        if (testClientId == null) return;
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, "WrongOld", "NewPass123!");
        });
    }

    @Test
    @Order(19)
    @DisplayName("Update with no changes should throw exception")
    void testUpdateNoChanges() {
        if (testClientId == null) return;
        Client client = clientService.getById(testClientId);
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.update(testClientId, client.getName(), client.getEmail(), client.getAddress());
        });
    }

    @Test
    @Order(20)
    @DisplayName("Get by email")
    void testGetByEmail() {
        // Убеждаемся, что клиент зарегистрирован с правильным email
        if (testClientId == null || testEmail == null || testEmail.contains("@test.com") && !testEmail.contains("email")) {
            Address address = Address.builder()
                    .country("Russia").city("Moscow").street("Test").building("1")
                    .apartment("10").entrance("1").floor(1)
                    .latitude(55.7558).longitude(37.6173).build();
            long suffix = System.currentTimeMillis() % 10000000000L;
            String phone = "+7" + String.format("%010d", suffix).substring(0, 10);
            String email = "test_email_" + System.currentTimeMillis() + "@test.com";
            Client client = clientService.register(phone, "Password123!", "Test", email, address);
            testClientId = client.getId();
            testEmail = client.getEmail(); // Используем email из зарегистрированного клиента
        }
        Client found = clientService.getByEmail(testEmail);
        assertNotNull(found, "Client should be found by email: " + testEmail);
        assertEquals(testClientId, found.getId());
    }

    @Test
    @Order(21)
    @DisplayName("Get by email - not found")
    void testGetByEmail_NotFound() {
        Client found = clientService.getByEmail("nonexistent@test.com");
        assertNull(found);
    }

    @Test
    @Order(22)
    @DisplayName("Get by phone - not found")
    void testGetByPhone_NotFound() {
        // Используем уникальный номер, который точно не существует
        String uniquePhone = "+7999" + (System.currentTimeMillis() % 10000000 + 10000000);
        Client found = clientService.getByPhone(uniquePhone);
        assertNull(found);
    }

    @Test
    @Order(23)
    @DisplayName("Get by ID - not found")
    void testGetById_NotFound() {
        Client found = clientService.getById(-1L);
        assertNull(found);
    }

    @Test
    @Order(24)
    @DisplayName("Deactivate - client not found")
    void testDeactivate_NotFound() {
        boolean result = clientService.deactivate(-1L);
        assertFalse(result);
    }

    @Test
    @Order(25)
    @DisplayName("Activate - client not found")
    void testActivate_NotFound() {
        boolean result = clientService.activate(-1L);
        assertFalse(result);
    }

    @Test
    @Order(26)
    @DisplayName("Change password - client not found")
    void testChangePassword_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(-1L, "OldPass123!", "NewPass123!");
        });
    }

    @Test
    @Order(27)
    @DisplayName("Change password - client inactive")
    void testChangePassword_Inactive() {
        if (testClientId == null) return;
        clientService.deactivate(testClientId);
        assertThrows(IllegalStateException.class, () -> {
            clientService.changePassword(testClientId, "Password123!", "NewPass123!");
        });
        clientService.activate(testClientId);
    }

    @Test
    @Order(28)
    @DisplayName("Change password - empty passwords")
    void testChangePassword_EmptyPasswords() {
        if (testClientId == null) return;
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, "", "NewPass123!");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, "Password123!", "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, null, "NewPass123!");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, "Password123!", null);
        });
    }

    @Test
    @Order(29)
    @DisplayName("Change password - weak new password")
    void testChangePassword_WeakPassword() {
        if (testClientId == null) return;
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.changePassword(testClientId, "Password123!", "weak");
        });
    }

    @Test
    @Order(30)
    @DisplayName("Update - client not found")
    void testUpdate_NotFound() {
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(RuntimeException.class, () -> {
            clientService.update(-1L, "Name", "email@test.com", address);
        });
    }

    @Test
    @Order(31)
    @DisplayName("Update - invalid email")
    void testUpdate_InvalidEmail() {
        if (testClientId == null) return;
        Address address = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.update(testClientId, "Name", "invalid-email", address);
        });
    }

    @Test
    @Order(32)
    @DisplayName("Update - email already exists")
    void testUpdate_EmailExists() {
        if (testClientId == null) return;
        // Создаем второго клиента
        Address address1 = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        long suffix = System.currentTimeMillis() % 10000000000L;
        String phone2 = "+7" + String.format("%010d", suffix).substring(0, 10);
        String email2 = "test2_" + System.currentTimeMillis() + "@test.com";
        Client client2 = clientService.register(phone2, "Password123!", "Test2", email2, address1);
        
        // Пытаемся обновить первого клиента на email второго
        Address address2 = Address.builder()
                .country("Russia").city("Moscow").street("Test").building("1")
                .apartment("10").entrance("1").floor(1)
                .latitude(55.7558).longitude(37.6173).build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.update(testClientId, "Name", email2, address2);
        });
        
        // Cleanup
        clientService.deactivate(client2.getId());
    }

    @Test
    @Order(33)
    @DisplayName("Update - invalid address")
    void testUpdate_InvalidAddress() {
        if (testClientId == null) return;
        Address invalidAddress = Address.builder()
                .country("").city("").street("").building("").build();
        assertThrows(IllegalArgumentException.class, () -> {
            clientService.update(testClientId, "Name", testEmail, invalidAddress);
        });
    }

    @Test
    @Order(34)
    @DisplayName("Add order history entry - client not found")
    void testAddOrderHistoryEntry_NotFound() {
        // Метод выбрасывает IllegalStateException, если клиент не найден
        assertThrows(IllegalStateException.class, () -> {
            clientService.addOrderHistoryEntry(-1L, "Test entry");
        });
    }

    @Test
    @Order(35)
    @DisplayName("Get order history - client not found")
    void testGetOrderHistory_NotFound() {
        List<String> history = clientService.getOrderHistory(-1L);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}

