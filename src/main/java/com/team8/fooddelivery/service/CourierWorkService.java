package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.order.Order;
import java.util.List;

public interface CourierWorkService {
  boolean acceptOrder(Long courierId, Long orderId);
  void pickupOrder(Long courierId, Long orderId);
  void completeOrder(Long courierId, Long orderId);
  List<Order> getOrderHistory(Long courierId);
  void startShift(Long courierId);
  void endShift(Long courierId);
}
