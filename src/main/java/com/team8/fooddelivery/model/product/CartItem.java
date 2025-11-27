package com.team8.fooddelivery.model.product;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}



