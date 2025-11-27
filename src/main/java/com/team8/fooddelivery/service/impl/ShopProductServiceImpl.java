package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.ProductRepository;
import com.team8.fooddelivery.repository.ShopRepository;
import com.team8.fooddelivery.service.ShopProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class ShopProductServiceImpl implements ShopProductService {

  private static final Logger logger = LoggerFactory.getLogger(ShopProductServiceImpl.class);
  private final ProductRepository productRepository = new ProductRepository();
  private final ShopRepository shopRepository = new ShopRepository();

  @Override
  public List<Product> getProductsByCategory(Long shopId, ProductCategory category) {
    try {
      return productRepository.findByShopIdAndCategory(shopId, category);
    } catch (SQLException e) {
      logger.error("Ошибка при получении продуктов по категории", e);
      return new ArrayList<>();
    }
  }

  @Override
  public void updateProductAvailability(Long shopId, Long productId, boolean isAvailable) {
    try {
      Optional<Product> productOpt = productRepository.findById(productId);
      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        product.setAvailable(isAvailable);
        productRepository.update(product);
      } else {
        logger.warn("Продукт не найден: productId={}", productId);
      }
    } catch (SQLException e) {
      logger.error("Ошибка при обновлении доступности продукта", e);
    }
  }

  @Override
  public void changeShopStatus(Long shopId, ShopStatus status) {
    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isPresent()) {
        Shop shop = shopOpt.get();
        shop.setStatus(status);
        shopRepository.update(shop);
      } else {
        logger.warn("Магазин не найден: shopId={}", shopId);
      }
    } catch (SQLException e) {
      logger.error("Ошибка при изменении статуса магазина", e);
    }
  }

  @Override
  public Shop getShopById(Long shopId) {
    try {
      return shopRepository.findById(shopId).orElse(null);
    } catch (SQLException e) {
      logger.error("Ошибка при получении магазина", e);
      return null;
    }
  }

  @Override
  public Shop updateShopInfo(Long shopId, Shop shop) {
    try {
      shop.setShopId(shopId);
      shopRepository.update(shop);
      return shop;
    } catch (SQLException e) {
      logger.error("Ошибка при обновлении информации о магазине", e);
      throw new RuntimeException("Не удалось обновить информацию о магазине", e);
    }
  }

  @Override
  public Product addProduct(Long shopId, Product product) {
    try {
      Long productId = productRepository.saveForShop(shopId, product);
      product.setProductId(productId);
      return product;
    } catch (SQLException e) {
      logger.error("Ошибка при добавлении продукта", e);
      throw new RuntimeException("Не удалось добавить продукт", e);
    }
  }

  @Override
  public Product updateProduct(Long shopId, Long productId, Product product) {
    try {
      product.setProductId(productId);
      productRepository.update(product);
      return product;
    } catch (SQLException e) {
      logger.error("Ошибка при обновлении продукта", e);
      throw new RuntimeException("Не удалось обновить продукт", e);
    }
  }

  @Override
  public void deleteProduct(Long shopId, Long productId) {
    try {
      productRepository.delete(productId);
    } catch (SQLException e) {
      logger.error("Ошибка при удалении продукта", e);
      throw new RuntimeException("Не удалось удалить продукт", e);
    }
  }

  @Override
  public List<Product> getShopProducts(Long shopId) {
    try {
      return productRepository.findByShopId(shopId);
    } catch (SQLException e) {
      logger.error("Ошибка при получении продуктов магазина", e);
      return new ArrayList<>();
    }
  }
}