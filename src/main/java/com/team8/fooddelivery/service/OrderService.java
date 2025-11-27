package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;

public interface OrderService {
    Order placeOrder(Long clientId, Address deliveryAddress, PaymentMethodForOrder paymentMethod);
}
