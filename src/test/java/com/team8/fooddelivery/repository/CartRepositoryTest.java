package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Тесты CartRepository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartRepositoryTest {

    private static CartRepository cartRepository;
    private static ClientRepository clientRepository;
    private static ProductRepository productRepository;
    private static ShopRepository shopRepository;
    private static Long testClientId;
    private static Long testCartId;
    private static Long testShopId;
    private static Long testProductId;

    @BeforeAll
    static void setUp() throws SQLException {
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

        if (!DatabaseConnection.testConnection()) {
            throw new RuntimeException("Не удалось подключиться к базе данных");
        }

        cartRepository = new CartRepository();
        clientRepository = new ClientRepository();
        productRepository = new ProductRepository();
        shopRepository = new ShopRepository();

        long suffix = System.currentTimeMillis();

        // Создаем тестового клиента для корзины
        Client testClient = Client.builder()
                .name("Тест Клиент для Корзины " + suffix)
                .phone("+7999" + (suffix % 1_000_0000))
                .email("carttest" + suffix + "@example.com")
                .passwordHash("hash")
                .status(ClientStatus.ACTIVE)
                .isActive(true)
                .orderHistory(List.of())
                .build();
        testClientId = clientRepository.save(testClient);

        // Создаем тестовый магазин и продукт
        Shop testShop = new Shop();
        testShop.setNaming("Тестовый Магазин " + suffix);
        testShop.setEmailForAuth("testshop" + suffix + "@example.com");
        testShop.setPhoneForAuth("+7888" + (suffix % 1_000_0000));
        testShop.setStatus(ShopStatus.APPROVED);
        testShop.setPassword("password");
        testShopId = shopRepository.save(testShop);

        Product testProduct = new Product(
                null,
                "Тестовый продукт",
                "Описание",
                100.0,
                200.0,
                ProductCategory.OTHER,
                true,
                java.time.Duration.ofMinutes(10)
        );
        testProductId = productRepository.saveForShop(testShopId, testProduct);
    }

    @Test
    @Order(1)
    @DisplayName("Создание корзины для клиента")
    void testSaveCart() throws SQLException {
        Cart cart = Cart.builder()
                .clientId(testClientId)
                .items(List.of())
                .build();

        testCartId = cartRepository.save(cart);
        assertNotNull(testCartId);
        assertTrue(testCartId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Поиск корзины по ID клиента")
    void testFindByClientId() throws SQLException {
        assumeTrue(testCartId != null);

        Optional<Cart> cartOpt = cartRepository.findByClientId(testClientId);
        assertTrue(cartOpt.isPresent());
        assertEquals(testClientId, cartOpt.get().getClientId());
    }

    @Test
    @Order(3)
    @DisplayName("Добавление элемента в корзину")
    void testSaveCartItem() throws SQLException {
        assumeTrue(testCartId != null);

        CartItem item = CartItem.builder()
                .cartId(testCartId)
                .productId(testProductId)
                .productName("Тестовый продукт")
                .quantity(2)
                .price(100.0)
                .build();

        Long itemId = cartRepository.saveCartItem(item);
        assertNotNull(itemId);
        assertTrue(itemId > 0);
    }

    @Test
    @Order(4)
    @DisplayName("Получение элементов корзины")
    void testFindCartItems() throws SQLException {
        assumeTrue(testCartId != null);

        List<CartItem> items = cartRepository.findCartItemsByCartId(testCartId);
        assertNotNull(items);
        assertFalse(items.isEmpty(), "В корзине должен быть хотя бы один элемент");
    }

    @Test
    @Order(5)
    @DisplayName("Очистка корзины")
    void testClearCart() throws SQLException {
        assumeTrue(testCartId != null);

        cartRepository.clearCart(testCartId);
        List<CartItem> items = cartRepository.findCartItemsByCartId(testCartId);
        assertTrue(items.isEmpty(), "Корзина должна быть пустой");
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Очистка тестовых данных
        if (testCartId != null) {
            try {
                cartRepository.delete(testCartId);
            } catch (SQLException e) {
                // Игнорируем ошибки при удалении
            }
        }
        if (testProductId != null) {
            try {
                productRepository.delete(testProductId);
            } catch (SQLException e) {
                // Игнорируем ошибки при удалении
            }
        }
        if (testShopId != null) {
            try {
                shopRepository.delete(testShopId);
            } catch (SQLException e) {
                // Игнорируем ошибки при удалении
            }
        }
        if (testClientId != null) {
            try {
                clientRepository.delete(testClientId);
            } catch (SQLException e) {
                // Игнорируем ошибки при удалении
            }
        }
    }
}

