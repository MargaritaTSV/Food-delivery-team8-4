package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.courier.Courier;

public interface CourierManagementService {
  Long registerNewCourier(String name, String phoneNumber, String password, String transportType);
  void updateCourierData(Long courierId, String name, String phoneNumber, String password, String transportType);
  Courier login(String phoneNumber, String password);
  Courier getCourierById(Long courierId);

}
