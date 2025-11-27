package com.team8.fooddelivery.model.order;

import java.time.Duration;
import java.util.List;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import lombok.*;

@Data
@AllArgsConstructor
public class OrderForClients {
    private Long orderId;
    private Long shopId;
    private Long clientId; // Long clientId
    private List<Product> items;
    private Address deliveryAddress;
    private OrderStatus status;
    private Double totalAmount;
    private Double storeRevenue;
    private Duration orderTime;
    private Duration estimatedReadyTime;  // Instant
    private Duration actualReadyTime;
    private String customerNotes;
    private String internalNotes;
    private String rejectionReason;
    private Duration preparationTime;   // Instant
    private boolean isPaid;
    private PaymentMethodForOrder paymentMethod; // enum
}

