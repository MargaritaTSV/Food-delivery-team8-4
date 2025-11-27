package com.team8.fooddelivery.model.client;

import java.time.Instant;
import java.util.*;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Cart;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    private Long id;
    private String name;
    private String phone;
    private String passwordHash;
    private String email;

    private Address address;
    private ClientStatus status;
    private Instant createdAt = Instant.now();

    private boolean isActive = true;
    private Cart cart;

    private List<String> orderHistory;
}


