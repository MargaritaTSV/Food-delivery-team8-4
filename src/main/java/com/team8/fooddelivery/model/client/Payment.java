package com.team8.fooddelivery.model.client;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long id;
    private Long orderId;
    private Double amount;
    private PaymentMethodForOrder method;
    private PaymentStatus status;
    private Instant createdAt;
}
