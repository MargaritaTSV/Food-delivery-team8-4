package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.ProductRepository;
import com.team8.fooddelivery.repository.ShopRepository;
import com.team8.fooddelivery.util.DatabaseConnection;
import com.team8.fooddelivery.util.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopProductServiceImplErrorHandlingTest {

    private ShopProductServiceImpl shopProductService;
    private ShopRepository shopRepository;
    private ProductRepository productRepository;
    private Long testShopId;
    private Long testProductId;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseInitializer.initializeDatabase();
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
        
        shopProductService = new ShopProductServiceImpl();
        shopRepository = new ShopRepository();
        productRepository = new ProductRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testProductId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
                stmt.setLong(1, testProductId);
                stmt.executeUpdate();
            }
        }
        if (testShopId != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM shops WHERE shop_id = ?")) {
                stmt.setLong(1, testShopId);
                stmt.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("getShopProducts: Should return empty list for non-existent shop")
    void testGetShopProducts_NotFound() {
        List<Product> result = shopProductService.getShopProducts(999999L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getProductsByCategory: Should return empty list for non-existent shop")
    void testGetProductsByCategory_NotFound() {
        List<Product> result = shopProductService.getProductsByCategory(999999L, ProductCategory.MAIN_DISH);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("updateProductAvailability: Should handle when product not found")
    void testUpdateProductAvailability_ProductNotFound() {
        assertDoesNotThrow(() -> {
            shopProductService.updateProductAvailability(1L, 999999L, true);
        });
    }

    @Test
    @DisplayName("changeShopStatus: Should handle when shop not found")
    void testChangeShopStatus_ShopNotFound() {
        assertDoesNotThrow(() -> {
            shopProductService.changeShopStatus(999999L, ShopStatus.PENDING);
        });
    }

    @Test
    @DisplayName("getShopById: Should return null for non-existent shop")
    void testGetShopById_NotFound() {
        Shop result = shopProductService.getShopById(999999L);
        assertNull(result);
    }

    @Test
    @DisplayName("getShopById: Should return shop when exists")
    void testGetShopById_Success() throws SQLException {
        // Create test shop
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + System.currentTimeMillis());
        shop.setDescription("Test Description");
        shop.setStatus(ShopStatus.APPROVED);
        shop.setEmailForAuth("test_" + System.currentTimeMillis() + "@test.com");
        shop.setPhoneForAuth("+7999" + (System.currentTimeMillis() % 10000000));
        shop.setPassword("test_password");
        
        testShopId = shopRepository.save(shop);
        
        Shop result = shopProductService.getShopById(testShopId);
        assertNotNull(result);
        assertEquals(testShopId, result.getShopId());
    }

    @Test
    @DisplayName("updateShopInfo: Should update shop when exists")
    void testUpdateShopInfo_Success() throws SQLException {
        // Create test shop
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + System.currentTimeMillis());
        shop.setDescription("Test Description");
        shop.setStatus(ShopStatus.APPROVED);
        shop.setEmailForAuth("test_" + System.currentTimeMillis() + "@test.com");
        shop.setPhoneForAuth("+7999" + (System.currentTimeMillis() % 10000000));
        shop.setPassword("test_password");
        
        testShopId = shopRepository.save(shop);
        
        Shop updatedShop = new Shop();
        updatedShop.setShopId(testShopId);
        updatedShop.setNaming("Updated Shop");
        
        Shop result = shopProductService.updateShopInfo(testShopId, updatedShop);
        assertNotNull(result);
        assertEquals("Updated Shop", result.getNaming());
    }

    @Test
    @DisplayName("addProduct: Should add product when shop exists")
    void testAddProduct_Success() throws SQLException {
        // Create test shop
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + System.currentTimeMillis());
        shop.setDescription("Test Description");
        shop.setStatus(ShopStatus.APPROVED);
        shop.setEmailForAuth("test_" + System.currentTimeMillis() + "@test.com");
        shop.setPhoneForAuth("+7999" + (System.currentTimeMillis() % 10000000));
        shop.setPassword("test_password");
        
        testShopId = shopRepository.save(shop);
        
        Product product = new Product(null, "Test Product", "Description", 100.0, 10.0, 
                ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        
        Product result = shopProductService.addProduct(testShopId, product);
        assertNotNull(result);
        assertNotNull(result.getProductId());
        testProductId = result.getProductId();
    }

    @Test
    @DisplayName("updateProduct: Should update product when exists")
    void testUpdateProduct_Success() throws SQLException {
        // Create test shop and product
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + System.currentTimeMillis());
        shop.setDescription("Test Description");
        shop.setStatus(ShopStatus.APPROVED);
        shop.setEmailForAuth("test_" + System.currentTimeMillis() + "@test.com");
        shop.setPhoneForAuth("+7999" + (System.currentTimeMillis() % 10000000));
        shop.setPassword("test_password");
        
        testShopId = shopRepository.save(shop);
        
        Product product = new Product(null, "Test Product", "Description", 100.0, 10.0, 
                ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        
        Product savedProduct = shopProductService.addProduct(testShopId, product);
        testProductId = savedProduct.getProductId();
        
        Product updatedProduct = new Product(testProductId, "Updated Product", "Updated Description", 
                150.0, 15.0, ProductCategory.DESSERT, false, Duration.ofMinutes(15));
        
        Product result = shopProductService.updateProduct(testShopId, testProductId, updatedProduct);
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
    }

    @Test
    @DisplayName("deleteProduct: Should delete product when exists")
    void testDeleteProduct_Success() throws SQLException {
        // Create test shop and product
        Shop shop = new Shop();
        shop.setNaming("Test Shop " + System.currentTimeMillis());
        shop.setDescription("Test Description");
        shop.setStatus(ShopStatus.APPROVED);
        shop.setEmailForAuth("test_" + System.currentTimeMillis() + "@test.com");
        shop.setPhoneForAuth("+7999" + (System.currentTimeMillis() % 10000000));
        shop.setPassword("test_password");
        
        testShopId = shopRepository.save(shop);
        
        Product product = new Product(null, "Test Product", "Description", 100.0, 10.0, 
                ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        
        Product savedProduct = shopProductService.addProduct(testShopId, product);
        Long productId = savedProduct.getProductId();
        
        assertDoesNotThrow(() -> {
            shopProductService.deleteProduct(testShopId, productId);
        });
        
        // Verify product is deleted
        assertTrue(productRepository.findById(productId).isEmpty());
    }
}
