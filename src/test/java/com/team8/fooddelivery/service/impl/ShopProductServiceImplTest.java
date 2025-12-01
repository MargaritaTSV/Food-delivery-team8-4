package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.model.shop.ShopType;
import com.team8.fooddelivery.model.shop.WorkingHours;
import com.team8.fooddelivery.repository.AddressRepository;
import com.team8.fooddelivery.repository.ShopRepository;
import com.team8.fooddelivery.repository.WorkingHoursRepository;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShopProductServiceImplTest {

    private ShopProductServiceImpl shopProductService;
    private static Long testShopId;
    private static Long testProductId;
    private static Long testAddressId;
    private static Long testWorkingHoursId;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        DatabaseConnection.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

        // Создаем тестовый магазин
        AddressRepository addrRepo = new AddressRepository();
        WorkingHoursRepository hoursRepo = new WorkingHoursRepository();
        ShopRepository shopRepo = new ShopRepository();

        Address address = Address.builder()
                .country("Russia")
                .city("Test City")
                .street("Test Street")
                .building("1")
                .build();
        testAddressId = addrRepo.save(address);

        WorkingHours workingHours = new WorkingHours(
                "09:00-21:00", "09:00-21:00", "09:00-21:00", "09:00-21:00",
                "09:00-21:00", "10:00-22:00", "10:00-22:00"
        );
        testWorkingHoursId = hoursRepo.save(workingHours);

        long suffix = System.currentTimeMillis();
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + suffix);
        shop.setEmailForAuth("shop" + suffix + "@test.com");
        shop.setPhoneForAuth("+7999" + (suffix % 10000000));
        shop.setStatus(ShopStatus.APPROVED);
        shop.setType(ShopType.RESTAURANT);
        shop.setPassword("test_password");
        shop.setAddress(address);
        shop.setWorkingHours(workingHours);

        testShopId = shopRepo.save(shop);
    }

    @AfterAll
    static void cleanup() {
        if (testProductId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                stmt.setLong(1, testProductId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignore
            }
        }
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
        shopProductService = new ShopProductServiceImpl();
    }

    @Test
    @Order(1)
    @DisplayName("Get shop by ID")
    void testGetShopById() {
        Shop shop = shopProductService.getShopById(testShopId);
        assertNotNull(shop);
        assertEquals(testShopId, shop.getShopId());
    }

    @Test
    @Order(2)
    @DisplayName("Get shop by ID - not found")
    void testGetShopById_NotFound() {
        Shop shop = shopProductService.getShopById(-1L);
        assertNull(shop);
    }

    @Test
    @Order(3)
    @DisplayName("Add product to shop")
    void testAddProduct() {
        Product product = new Product(null, "Test Product", "Description",
                100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(15));
        Product added = shopProductService.addProduct(testShopId, product);
        assertNotNull(added);
        assertNotNull(added.getProductId());
        testProductId = added.getProductId();
    }

    @Test
    @Order(4)
    @DisplayName("Get shop products")
    void testGetShopProducts() {
        List<Product> products = shopProductService.getShopProducts(testShopId);
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Get products by category")
    void testGetProductsByCategory() {
        List<Product> products = shopProductService.getProductsByCategory(testShopId, ProductCategory.MAIN_DISH);
        assertNotNull(products);
    }

    @Test
    @Order(6)
    @DisplayName("Update product availability")
    void testUpdateProductAvailability() {
        if (testProductId == null) {
            Product product = new Product(null, "Test Product 2", "Description",
                    100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(15));
            Product added = shopProductService.addProduct(testShopId, product);
            testProductId = added.getProductId();
        }
        shopProductService.updateProductAvailability(testShopId, testProductId, false);
        // Verify by getting product
        Product product = shopProductService.getShopProducts(testShopId).stream()
                .filter(p -> p.getProductId().equals(testProductId))
                .findFirst()
                .orElse(null);
        assertNotNull(product);
        assertFalse(product.getAvailable());
    }

    @Test
    @Order(7)
    @DisplayName("Update product")
    void testUpdateProduct() {
        if (testProductId == null) {
            Product product = new Product(null, "Test Product 3", "Description",
                    100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(15));
            Product added = shopProductService.addProduct(testShopId, product);
            testProductId = added.getProductId();
        }
        Product product = new Product(testProductId, "Updated Product", "Updated Description",
                150.0, 15.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(20));
        Product updated = shopProductService.updateProduct(testShopId, testProductId, product);
        assertNotNull(updated);
        assertEquals("Updated Product", updated.getName());
    }

    @Test
    @Order(8)
    @DisplayName("Delete product")
    void testDeleteProduct() {
        if (testProductId == null) {
            Product product = new Product(null, "Test Product 4", "Description",
                    100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(15));
            Product added = shopProductService.addProduct(testShopId, product);
            testProductId = added.getProductId();
        }
        assertDoesNotThrow(() -> shopProductService.deleteProduct(testShopId, testProductId));
    }

    @Test
    @Order(9)
    @DisplayName("Change shop status")
    void testChangeShopStatus() {
        shopProductService.changeShopStatus(testShopId, ShopStatus.REJECTED);
        Shop shop = shopProductService.getShopById(testShopId);
        assertEquals(ShopStatus.REJECTED, shop.getStatus());
        // Restore
        shopProductService.changeShopStatus(testShopId, ShopStatus.APPROVED);
    }

    @Test
    @Order(10)
    @DisplayName("Update shop info")
    void testUpdateShopInfo() {
        Shop shop = shopProductService.getShopById(testShopId);
        shop.setNaming("Updated Shop Name");
        Shop updated = shopProductService.updateShopInfo(testShopId, shop);
        assertNotNull(updated);
        assertEquals("Updated Shop Name", updated.getNaming());
    }

    @Test
    @Order(11)
    @DisplayName("Get products by category - empty result")
    void testGetProductsByCategory_Empty() {
        List<Product> products = shopProductService.getProductsByCategory(testShopId, ProductCategory.DRINK);
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("Update product availability - product not found")
    void testUpdateProductAvailability_NotFound() {
        assertDoesNotThrow(() -> {
            shopProductService.updateProductAvailability(testShopId, -1L, true);
        });
    }

    @Test
    @Order(13)
    @DisplayName("Get shop products - empty")
    void testGetShopProducts_Empty() {
        // Test with non-existent shop
        List<Product> products = shopProductService.getShopProducts(-1L);
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("Change shop status - shop not found")
    void testChangeShopStatus_NotFound() {
        // This should not throw, just log warning
        assertDoesNotThrow(() -> {
            shopProductService.changeShopStatus(-1L, ShopStatus.PENDING);
        });
    }

    @Test
    @Order(15)
    @DisplayName("Get products by category - SQLException handling")
    void testGetProductsByCategory_SQLException() {
        // Test with invalid shop ID that might cause issues
        List<Product> products = shopProductService.getProductsByCategory(-999L, ProductCategory.MAIN_DISH);
        assertNotNull(products);
        // Should return empty list on error
    }

    @Test
    @Order(16)
    @DisplayName("Update product availability - product not found path")
    void testUpdateProductAvailability_ProductNotFound() {
        // This covers the else branch when product is not found
        assertDoesNotThrow(() -> {
            shopProductService.updateProductAvailability(testShopId, -999L, true);
        });
    }

    @Test
    @Order(17)
    @DisplayName("Change shop status - shop not found path")
    void testChangeShopStatus_ShopNotFound() {
        // This covers the else branch when shop is not found
        assertDoesNotThrow(() -> {
            shopProductService.changeShopStatus(-999L, ShopStatus.PENDING);
        });
    }

    @Test
    @Order(18)
    @DisplayName("Get shop by ID - SQLException handling")
    void testGetShopById_SQLException() {
        // Test with invalid ID
        Shop shop = shopProductService.getShopById(-999L);
        assertNull(shop);
    }
}

