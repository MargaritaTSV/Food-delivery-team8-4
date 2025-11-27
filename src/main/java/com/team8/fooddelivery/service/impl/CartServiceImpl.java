package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.repository.CartRepository;
import com.team8.fooddelivery.repository.ClientRepository;
import com.team8.fooddelivery.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartRepository cartRepository = new CartRepository();
    private final ClientRepository clientRepository = new ClientRepository();

    // Используется ClientService при регистрации
    public Cart createCartForClient(Long clientId) {
        if (clientId == null) {
            // Временная корзина, будет сохранена позже
            return Cart.builder()
                    .clientId(null)
                    .items(new ArrayList<>())
                    .build();
        }

        validateClient(clientId);

        try {
            // Проверяем, есть ли уже корзина для этого клиента
            Optional<Cart> existingCart = cartRepository.findByClientId(clientId);
            if (existingCart.isPresent()) {
                return existingCart.get();
            }

            // Создаем новую корзину
            Cart cart = Cart.builder()
                    .clientId(clientId)
                    .items(new ArrayList<>())
                    .build();

            Long cartId = cartRepository.save(cart);
            cart.setId(cartId);
            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при создании корзины для клиента {}", clientId, e);
            throw new RuntimeException("Не удалось создать корзину", e);
        }
    }

    @Override
    public Cart getCartForClient(Long clientId) {
        validateClient(clientId);
        try {
            return cartRepository.findByClientId(clientId).orElse(null);
        } catch (SQLException e) {
            logger.error("Ошибка при получении корзины для клиента {}", clientId, e);
            return null;
        }
    }

    @Override
    public List<CartItem> listItems(Long clientId) {
        Cart cart = getCartForClient(clientId);
        if (cart == null) return Collections.emptyList();
        return cart.getItems();
    }

    @Override
    public Cart addItem(Long clientId, CartItem item) {
        try {
            Cart cart = getOrCreate(clientId);

            // Если товар уже есть — увеличиваем количество
            Optional<CartItem> existing = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(item.getProductId()))
                    .findFirst();

            if (existing.isPresent()) {
                CartItem ci = existing.get();
                ci.setQuantity(ci.getQuantity() + item.getQuantity());
                cartRepository.updateCartItem(ci);
                return cart;
            }

            // Новый товар
            item.setCartId(cart.getId());
            Long itemId = cartRepository.saveCartItem(item);
            item.setId(itemId);
            cart.getItems().add(item);
            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении товара в корзину", e);
            throw new RuntimeException("Не удалось добавить товар в корзину", e);
        }
    }

    @Override
    public Cart updateItem(Long clientId, Long productId, int newQuantity) {
        try {
            Cart cart = getOrCreate(clientId);

            Optional<CartItem> itemOpt = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst();

            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                item.setQuantity(newQuantity);
                cartRepository.updateCartItem(item);
            }

            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении товара в корзине", e);
            throw new RuntimeException("Не удалось обновить товар в корзине", e);
        }
    }

    @Override
    public Cart removeItem(Long clientId, Long productId) {
        try {
            Cart cart = getOrCreate(clientId);
            Optional<CartItem> itemToRemove = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst();

            if (itemToRemove.isPresent()) {
                cartRepository.deleteCartItemByProductId(cart.getId(), productId);
                cart.getItems().removeIf(i -> i.getProductId().equals(productId));
            }
            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении товара из корзины", e);
            throw new RuntimeException("Не удалось удалить товар из корзины", e);
        }
    }

    @Override
    public Cart clear(Long clientId) {
        try {
            Cart cart = getOrCreate(clientId);
            cartRepository.clearCart(cart.getId());
            cart.getItems().clear();
            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при очистке корзины", e);
            throw new RuntimeException("Не удалось очистить корзину", e);
        }
    }

    @Override
    public Long calculateTotal(Long clientId) {
        Cart cart = getCartForClient(clientId);
        if (cart == null) return 0L;
        return cart.getTotalPrice(); // totalPrice = Long
    }

    // ---------------------------------
    //      PRIVATE HELPERS
    // ---------------------------------

    private Cart getOrCreate(Long clientId) {
        validateClient(clientId);
        try {
            Optional<Cart> existingCart = cartRepository.findByClientId(clientId);
            if (existingCart.isPresent()) {
                return existingCart.get();
            }

            // Создаем новую корзину
            Cart cart = Cart.builder()
                    .clientId(clientId)
                    .items(new ArrayList<>())
                    .build();

            Long cartId = cartRepository.save(cart);
            cart.setId(cartId);
            return cart;
        } catch (SQLException e) {
            logger.error("Ошибка при получении/создании корзины для клиента {}", clientId, e);
            throw new RuntimeException("Не удалось получить или создать корзину", e);
        }
    }

    private void validateClient(Long clientId) {
        try {
            if (clientId == null || clientRepository.findById(clientId).isEmpty()) {
                throw new IllegalArgumentException("Клиент не найден или не задан");
            }
        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования клиента {}", clientId, e);
            throw new RuntimeException("Не удалось проверить клиента", e);
        }
    }
}

