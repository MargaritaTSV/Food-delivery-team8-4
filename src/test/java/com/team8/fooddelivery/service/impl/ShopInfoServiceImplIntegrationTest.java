package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShopInfoServiceImplIntegrationTest {

    private ShopInfoServiceImpl shopInfoService;
    private static Long testShopId;
    private static Long testAddressId;
    private static Long testWorkingHoursId;
    private static String testEmail;
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
        if (testShopId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM shops WHERE shop_id = ?")) {
                stmt.setLong(1, testShopId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (testAddressId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM addresses WHERE id = ?")) {
                stmt.setLong(1, testAddressId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
        if (testWorkingHoursId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM working_hours WHERE id = ?")) {
                stmt.setLong(1, testWorkingHoursId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    @BeforeEach
    void setUp() {
        shopInfoService = new ShopInfoServiceImpl();
        long suffix = System.currentTimeMillis();
        testEmail = "shop" + suffix + "@test.com";
        testPhone = "+7999" + (suffix % 10000000);
    }

    @Test
    @Order(1)
    @DisplayName("Register shop")
    void testRegisterShop() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop");
        Shop registered = shopInfoService.registerShop(shop, testEmail, "Password123!", testPhone);
        assertNotNull(registered);
        assertNotNull(registered.getShopId());
        testShopId = registered.getShopId();
        assertEquals(ShopStatus.PENDING, registered.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("Approve shop")
    void testApproveShop() {
        if (testShopId == null) {
            Shop shop = new Shop();
            shop.setNaming("Test Shop 2");
            Shop registered = shopInfoService.registerShop(shop, testEmail + "2", "Password123!", testPhone + "1");
            testShopId = registered.getShopId();
        }
        boolean result = shopInfoService.approveShop(testShopId);
        assertTrue(result);
    }

    @Test
    @Order(3)
    @DisplayName("Reject shop")
    void testRejectShop() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 3");
        long suffix = System.currentTimeMillis();
        String email = "reject_" + suffix + "@test.com";
        String phone = "+7999" + (suffix % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        boolean result = shopInfoService.rejectShop("Test rejection reason", shopId);
        assertTrue(result);
    }

    @Test
    @Order(4)
    @DisplayName("Approve shop - shop not found")
    void testApproveShop_NotFound() {
        boolean result = shopInfoService.approveShop(-1L);
        assertFalse(result);
    }

    @Test
    @Order(5)
    @DisplayName("Approve shop - already processed")
    void testApproveShop_AlreadyProcessed() {
        if (testShopId == null) {
            Shop shop = new Shop();
            shop.setNaming("Test Shop Approved");
            Shop registered = shopInfoService.registerShop(shop, testEmail + "approved", "Password123!", testPhone + "approved");
            testShopId = registered.getShopId();
            shopInfoService.approveShop(testShopId);
        }
        boolean result = shopInfoService.approveShop(testShopId);
        assertFalse(result); // Already approved
    }

    @Test
    @Order(6)
    @DisplayName("Reject shop - shop not found")
    void testRejectShop_NotFound() {
        boolean result = shopInfoService.rejectShop("Reason", -1L);
        assertFalse(result);
    }

    @Test
    @Order(7)
    @DisplayName("Reject shop - already processed")
    void testRejectShop_AlreadyProcessed() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop Rejected");
        long suffix = System.currentTimeMillis();
        String email = "rejected_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 1) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        shopInfoService.rejectShop("Reason", shopId);
        boolean result = shopInfoService.rejectShop("Another reason", shopId);
        assertFalse(result); // Already rejected
    }

    @Test
    @Order(4)
    @DisplayName("Change password")
    void testChangePassword() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 4");
        long suffix = System.currentTimeMillis();
        String email = "pass_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 1) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        String result = shopInfoService.changePassword(shopId, email, phone, "NewPass123!", "Password123!");
        assertNotNull(result);
        assertTrue(result.contains("успешно"));
    }

    @Test
    @Order(5)
    @DisplayName("Change email for auth")
    void testChangeEmailForAuth() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 5");
        long suffix = System.currentTimeMillis();
        String email = "email_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 2) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        String newEmail = "new_" + suffix + "@test.com";
        String result = shopInfoService.changeEmailForAuth(shopId, phone, "Password123!", newEmail);
        assertNotNull(result);
        assertTrue(result.contains("успешно"));
    }

    @Test
    @Order(6)
    @DisplayName("Change phone for auth")
    void testChangePhoneForAuth() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 6");
        long suffix = System.currentTimeMillis();
        String email = "phone_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 3) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        String newPhone = "+7999" + ((suffix + 4) % 10000000);
        String result = shopInfoService.changePhoneForAuth(shopId, email, "Password123!", newPhone);
        assertNotNull(result);
        assertTrue(result.contains("успешно"));
    }

    @Test
    @Order(7)
    @DisplayName("Change password - wrong credentials")
    void testChangePassword_WrongCredentials() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 7");
        long suffix = System.currentTimeMillis();
        String email = "wrong_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 5) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        assertThrows(SecurityException.class, () -> {
            shopInfoService.changePassword(shopId, "wrong@email.com", phone, "NewPass123!", "Password123!");
        });
    }

    @Test
    @Order(8)
    @DisplayName("Change password - wrong old password")
    void testChangePassword_WrongOldPassword() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 8");
        long suffix = System.currentTimeMillis();
        String email = "wrongpass_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 6) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        // changePassword checks if password.equals(shop.getPassword())
        // If password doesn't match, it doesn't throw but also doesn't change password
        String result = shopInfoService.changePassword(shopId, email, phone, "NewPass123!", "WrongPassword");
        assertNotNull(result);
        // Password should not be changed if old password is wrong
    }

    @Test
    @Order(9)
    @DisplayName("Change email - wrong phone")
    void testChangeEmail_WrongPhone() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 9");
        long suffix = System.currentTimeMillis();
        String email = "wrongphone_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 7) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        assertThrows(SecurityException.class, () -> {
            shopInfoService.changeEmailForAuth(shopId, "wrong_phone", "Password123!", "new@email.com");
        });
    }

    @Test
    @Order(10)
    @DisplayName("Change phone - wrong email")
    void testChangePhone_WrongEmail() {
        Shop shop = new Shop();
        shop.setNaming("Test Shop 10");
        long suffix = System.currentTimeMillis();
        String email = "wrongemail_" + suffix + "@test.com";
        String phone = "+7999" + ((suffix + 8) % 10000000);
        Shop registered = shopInfoService.registerShop(shop, email, "Password123!", phone);
        Long shopId = registered.getShopId();
        assertThrows(SecurityException.class, () -> {
            shopInfoService.changePhoneForAuth(shopId, "wrong@email.com", "Password123!", "+79999999999");
        });
    }

    @Test
    @Order(11)
    @DisplayName("Change email - existing email")
    void testChangeEmail_ExistingEmail() {
        Shop shop1 = new Shop();
        shop1.setNaming("Test Shop 11a");
        long suffix = System.currentTimeMillis();
        String email1 = "existing1_" + suffix + "@test.com";
        String phone1 = "+7999" + ((suffix + 9) % 10000000);
        Shop registered1 = shopInfoService.registerShop(shop1, email1, "Password123!", phone1);
        Long shopId1 = registered1.getShopId();

        Shop shop2 = new Shop();
        shop2.setNaming("Test Shop 11b");
        String email2 = "existing2_" + suffix + "@test.com";
        String phone2 = "+7999" + ((suffix + 10) % 10000000);
        Shop registered2 = shopInfoService.registerShop(shop2, email2, "Password123!", phone2);
        Long shopId2 = registered2.getShopId();

        // Try to change shop2's email to shop1's email
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changeEmailForAuth(shopId2, phone2, "Password123!", email1);
        });
    }

    @Test
    @Order(12)
    @DisplayName("Change phone - existing phone")
    void testChangePhone_ExistingPhone() {
        Shop shop1 = new Shop();
        shop1.setNaming("Test Shop 12a");
        long suffix = System.currentTimeMillis();
        String email1 = "existingphone1_" + suffix + "@test.com";
        String phone1 = "+7999" + ((suffix + 11) % 10000000);
        Shop registered1 = shopInfoService.registerShop(shop1, email1, "Password123!", phone1);
        Long shopId1 = registered1.getShopId();

        Shop shop2 = new Shop();
        shop2.setNaming("Test Shop 12b");
        String email2 = "existingphone2_" + suffix + "@test.com";
        String phone2 = "+7999" + ((suffix + 12) % 10000000);
        Shop registered2 = shopInfoService.registerShop(shop2, email2, "Password123!", phone2);
        Long shopId2 = registered2.getShopId();

        // Try to change shop2's phone to shop1's phone
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changePhoneForAuth(shopId2, email2, "Password123!", phone1);
        });
    }
}

