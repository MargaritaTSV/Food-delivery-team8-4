package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.product.CartItem;
import java.util.List;

public interface CartService {

    Cart createCartForClient(Long clientId);

    Cart getCartForClient(Long clientId);

    Cart addItem(Long clientId, CartItem item);

    Cart removeItem(Long clientId, Long productId);

    Cart updateItem(Long clientId, Long productId, int newQuantity);

    List<CartItem> listItems(Long clientId);

    Cart clear(Long clientId);

    Long calculateTotal(Long clientId);
}



