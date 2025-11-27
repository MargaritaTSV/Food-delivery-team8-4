package com.team8.fooddelivery.model.product;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    private Long id;
    private Long clientId;
    private List<CartItem> items;

    public static Cart createEmpty(Long clientId) {
        return Cart.builder()
                .id(null)
                .clientId(clientId)
                .items(new ArrayList<>())
                .build();
    }

    public List<CartItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public Long getTotalPrice() {
        return getItems().stream()
                .mapToLong(i -> (long) (i.getPrice() * i.getQuantity()))
                .sum();
    }
}
