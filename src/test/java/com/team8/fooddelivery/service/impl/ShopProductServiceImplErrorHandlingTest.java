package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.ProductRepository;
import com.team8.fooddelivery.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ShopProductServiceImplErrorHandlingTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShopRepository shopRepository;

    private ShopProductServiceImpl shopProductService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        shopProductService = new ShopProductServiceImpl();
        // Use reflection to inject mocked repositories
        java.lang.reflect.Field productRepoField = ShopProductServiceImpl.class.getDeclaredField("productRepository");
        productRepoField.setAccessible(true);
        productRepoField.set(shopProductService, productRepository);

        java.lang.reflect.Field shopRepoField = ShopProductServiceImpl.class.getDeclaredField("shopRepository");
        shopRepoField.setAccessible(true);
        shopRepoField.set(shopProductService, shopRepository);
    }

    @Test
    @DisplayName("getShopProducts: Should return empty list on SQLException")
    void testGetShopProducts_SQLException() throws SQLException {
        when(productRepository.findByShopId(anyLong())).thenThrow(new SQLException("DB Error"));
        
        var result = shopProductService.getShopProducts(1L);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByShopId(anyLong());
    }

    @Test
    @DisplayName("getProductsByCategory: Should return empty list on SQLException")
    void testGetProductsByCategory_SQLException() throws SQLException {
        when(productRepository.findByShopIdAndCategory(anyLong(), any(ProductCategory.class)))
                .thenThrow(new SQLException("DB Error"));
        
        var result = shopProductService.getProductsByCategory(1L, ProductCategory.MAIN_DISH);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByShopIdAndCategory(anyLong(), any(ProductCategory.class));
    }

    @Test
    @DisplayName("updateProductAvailability: Should handle SQLException when product not found")
    void testUpdateProductAvailability_ProductNotFound() throws SQLException {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> {
            shopProductService.updateProductAvailability(1L, 999L, true);
        });
        
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("updateProductAvailability: Should handle SQLException on update")
    void testUpdateProductAvailability_SQLException() throws SQLException {
        Product product = new Product(1L, "Test", "Desc", 100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        doThrow(new SQLException("DB Error")).when(productRepository).update(any(Product.class));
        
        assertDoesNotThrow(() -> {
            shopProductService.updateProductAvailability(1L, 1L, false);
        });
        
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).update(any(Product.class));
    }

    @Test
    @DisplayName("changeShopStatus: Should handle SQLException when shop not found")
    void testChangeShopStatus_ShopNotFound() throws SQLException {
        when(shopRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> {
            shopProductService.changeShopStatus(999L, ShopStatus.PENDING);
        });
        
        verify(shopRepository, times(1)).findById(anyLong());
        verify(shopRepository, never()).update(any(Shop.class));
    }

    @Test
    @DisplayName("changeShopStatus: Should handle SQLException on update")
    void testChangeShopStatus_SQLException() throws SQLException {
        Shop shop = new Shop();
        shop.setShopId(1L);
        when(shopRepository.findById(anyLong())).thenReturn(Optional.of(shop));
        doThrow(new SQLException("DB Error")).when(shopRepository).update(any(Shop.class));
        
        assertDoesNotThrow(() -> {
            shopProductService.changeShopStatus(1L, ShopStatus.PENDING);
        });
        
        verify(shopRepository, times(1)).findById(anyLong());
        verify(shopRepository, times(1)).update(any(Shop.class));
    }

    @Test
    @DisplayName("getShopById: Should return null on SQLException")
    void testGetShopById_SQLException() throws SQLException {
        when(shopRepository.findById(anyLong())).thenThrow(new SQLException("DB Error"));
        
        Shop result = shopProductService.getShopById(1L);
        
        assertNull(result);
        verify(shopRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("updateShopInfo: Should throw RuntimeException on SQLException")
    void testUpdateShopInfo_SQLException() throws SQLException {
        Shop shop = new Shop();
        doThrow(new SQLException("DB Error")).when(shopRepository).update(any(Shop.class));
        
        assertThrows(RuntimeException.class, () -> {
            shopProductService.updateShopInfo(1L, shop);
        });
        
        verify(shopRepository, times(1)).update(any(Shop.class));
    }

    @Test
    @DisplayName("addProduct: Should throw RuntimeException on SQLException")
    void testAddProduct_SQLException() throws SQLException {
        Product product = new Product(null, "Test", "Desc", 100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        when(productRepository.saveForShop(anyLong(), any(Product.class)))
                .thenThrow(new SQLException("DB Error"));
        
        assertThrows(RuntimeException.class, () -> {
            shopProductService.addProduct(1L, product);
        });
        
        verify(productRepository, times(1)).saveForShop(anyLong(), any(Product.class));
    }

    @Test
    @DisplayName("updateProduct: Should throw RuntimeException on SQLException")
    void testUpdateProduct_SQLException() throws SQLException {
        Product product = new Product(1L, "Test", "Desc", 100.0, 10.0, ProductCategory.MAIN_DISH, true, Duration.ofMinutes(10));
        doThrow(new SQLException("DB Error")).when(productRepository).update(any(Product.class));
        
        assertThrows(RuntimeException.class, () -> {
            shopProductService.updateProduct(1L, 1L, product);
        });
        
        verify(productRepository, times(1)).update(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct: Should throw RuntimeException on SQLException")
    void testDeleteProduct_SQLException() throws SQLException {
        doThrow(new SQLException("DB Error")).when(productRepository).delete(anyLong());
        
        assertThrows(RuntimeException.class, () -> {
            shopProductService.deleteProduct(1L, 1L);
        });
        
        verify(productRepository, times(1)).delete(anyLong());
    }
}

