package com.team8.fooddelivery.model.courier;

import com.team8.fooddelivery.model.notification.Notification;
import lombok.Data;
import java.util.*;

@Data
public class Courier {

  private Long id;
  private String name;
  private String password;
  private String phoneNumber;
  private CourierStatus status;
  private String transportType;
  private Long currentOrderId;
  private Double currentBalance;
  private Long bankCard;
  private List<Notification> notifications = new ArrayList<>();

  public static final Map<Long, Courier> TEST_COURIERS = new HashMap<>();

  static {
    Courier c1 = new Courier();
    c1.setId(1L);
    c1.setName("Ivan Petrov");
    c1.setPassword("pass123");
    c1.setPhoneNumber("+79990001122");
    c1.setStatus(CourierStatus.ON_SHIFT);
    c1.setTransportType("bike");
    c1.setCurrentBalance(500.0);
    c1.setBankCard(1234567890123456L);

    Courier c2 = new Courier();
    c2.setId(2L);
    c2.setName("Anna Ivanova");
    c2.setPassword("securepass");
    c2.setPhoneNumber("+79995554433");
    c2.setStatus(CourierStatus.DELIVERING);
    c2.setTransportType("car");
    c2.setCurrentBalance(300.0);
    c2.setBankCard(9876543210123456L);

    TEST_COURIERS.put(c1.getId(), c1);
    TEST_COURIERS.put(c2.getId(), c2);
  }
}
