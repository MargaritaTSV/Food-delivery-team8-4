package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.model.shop.ShopStatus;

import java.util.List;

public interface ShopProductService {

  Shop getShopById(Long shopId);

  Shop updateShopInfo(Long shopId, Shop updatedShop); // + new Shop

  Product addProduct(Long shopId, Product product); // + набор полей

  Product updateProduct(Long shopId, Long productId, Product updatedProduct); // changed Integer to Long

  void deleteProduct(Long shopId, Long productId); // changed Integer to Long

  List<Product> getShopProducts(Long shopId);

  List<Product> getProductsByCategory(Long shopId, ProductCategory category);

  void updateProductAvailability(Long shopId, Long productId, boolean isAvailable); // changed Integer to Long

  void changeShopStatus(Long shopId, ShopStatus status);
}
