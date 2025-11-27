package com.team8.fooddelivery.model.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.CartItem;
import com.team8.fooddelivery.model.client.PaymentStatus;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import lombok.Data;

@Data
public class Order {
    private Long id;
    private OrderStatus status;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAddressId;
    private Address deliveryAddress;
    private Long courierId;
    private Double totalPrice;
    private List<CartItem> items = new ArrayList<>();
    private PaymentStatus paymentStatus;
    private PaymentMethodForOrder paymentMethod;
    private Instant createdAt;
    private Instant estimatedDeliveryTime;

    public static final Map<Long, Order> TEST_ORDERS = new HashMap<>();

    static {
        Order o1 = new Order();
        o1.setId(101L);
        o1.setStatus(OrderStatus.READY_FOR_PICKUP);
        o1.setCustomerId(1L);
        o1.setRestaurantId(1L);
        o1.setDeliveryAddress(Address.builder()
            .city("Москва")
            .street("Пушкина")
            .building("10")
            .build());
        o1.setTotalPrice(1500.0);
        o1.setItems(List.of(
                CartItem.builder().productName("Пицца Пепперони").quantity(1).price(1000.0).build(),
                CartItem.builder().productName("Кола").quantity(1).price(500.0).build()
        ));
        o1.setPaymentMethod(PaymentMethodForOrder.CASH);
        o1.setPaymentStatus(PaymentStatus.SUCCESS);

        Order o2 = new Order();
        o2.setId(102L);
        o2.setStatus(OrderStatus.PENDING);
        o2.setCustomerId(2L);
        o2.setRestaurantId(2L);
        o2.setDeliveryAddress(Address.builder()
            .city("Санкт-Петербург")
            .street("Ленина")
            .building("5")
            .build());
        o2.setTotalPrice(900.0);
        o2.setItems(List.of(CartItem.builder().productName("Вок с курицей").quantity(1).price(900.0).build()));
        o2.setPaymentMethod(PaymentMethodForOrder.ONLINE);
        o2.setPaymentStatus(PaymentStatus.SUCCESS);

        TEST_ORDERS.put(o1.getId(), o1);
        TEST_ORDERS.put(o2.getId(), o2);
    }
}
