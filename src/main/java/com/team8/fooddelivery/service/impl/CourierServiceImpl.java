package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.courier.CourierStatus;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.repository.CourierRepository;
import com.team8.fooddelivery.repository.OrderRepository;
import com.team8.fooddelivery.service.CourierManagementService;
import com.team8.fooddelivery.service.CourierWorkService;
import com.team8.fooddelivery.util.ValidationUtils;
import com.team8.fooddelivery.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CourierServiceImpl implements CourierManagementService, CourierWorkService {

    private static final Logger logger = LoggerFactory.getLogger(CourierServiceImpl.class);
    private final CourierRepository courierRepository = new CourierRepository();
    private final OrderRepository orderRepository = new OrderRepository();

    public Long registerNewCourier(String name, String phoneNumber, String password, String transportType) {
        if (!ValidationUtils.isValidPhone(phoneNumber)) {
            throw new IllegalArgumentException("Неверный формат номера телефона.");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            throw new IllegalArgumentException("Пароль не соответствует требованиям безопасности.");
        }

        try {
            Courier c = new Courier();
            c.setName(name);
            c.setPhoneNumber(phoneNumber);
            c.setPassword(PasswordUtils.hashPassword(password));
            c.setTransportType(transportType);
            c.setStatus(CourierStatus.OFF_SHIFT);
            c.setCurrentBalance(0.0);
            c.setBankCard(1111222233334444L);

            Long id = courierRepository.save(c);
            c.setId(id);
            return id;
        } catch (SQLException e) {
            logger.error("Ошибка при регистрации курьера", e);
            throw new RuntimeException("Не удалось зарегистрировать курьера", e);
        }
    }

    @Override
    public Courier login(String phoneNumber, String password) {
        try {
            Optional<Courier> courierOpt = courierRepository.findByPhoneNumber(phoneNumber);
            if (courierOpt.isEmpty()) {
                return null;
            }

            Courier c = courierOpt.get();
            if (PasswordUtils.checkPassword(password, c.getPassword())) {
                return c;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при входе курьера", e);
            return null;
        }
    }


    @Override
    public void updateCourierData(Long courierId, String name, String phoneNumber, String password, String transportType) {
        if (!ValidationUtils.isValidPhone(phoneNumber)) {
            throw new IllegalArgumentException("Неверный формат номера телефона.");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            throw new IllegalArgumentException("Пароль не соответствует требованиям безопасности.");
        }

        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            if (courierOpt.isEmpty()) {
                throw new IllegalArgumentException("Курьер не найден");
            }

            Courier c = courierOpt.get();
            c.setName(name);
            c.setPhoneNumber(phoneNumber);
            c.setPassword(PasswordUtils.hashPassword(password));
            c.setTransportType(transportType);
            courierRepository.update(c);
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении данных курьера", e);
            throw new RuntimeException("Не удалось обновить данные курьера", e);
        }
    }

    @Override
    public void startShift(Long courierId) {
        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            if (courierOpt.isPresent()) {
                Courier c = courierOpt.get();
                c.setStatus(CourierStatus.ON_SHIFT);
                courierRepository.update(c);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при начале смены", e);
        }
    }

    @Override
    public void endShift(Long courierId) {
        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            if (courierOpt.isPresent()) {
                Courier c = courierOpt.get();
                c.setStatus(CourierStatus.OFF_SHIFT);
                courierRepository.update(c);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при окончании смены", e);
        }
    }

    @Override
    public boolean acceptOrder(Long courierId, Long orderId) {
        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);

            if (courierOpt.isEmpty() || orderOpt.isEmpty()) {
                return false;
            }

            Courier c = courierOpt.get();
            Order o = orderOpt.get();

            if (c.getStatus() == CourierStatus.ON_SHIFT &&
                    o.getStatus() == OrderStatus.READY_FOR_PICKUP) {

                c.setStatus(CourierStatus.DELIVERING);
                c.setCurrentOrderId(orderId);
                courierRepository.update(c);

                o.setStatus(OrderStatus.DELIVERING);
                o.setCourierId(courierId);
                orderRepository.update(o);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка при принятии заказа", e);
            return false;
        }
    }

    @Override
    public void pickupOrder(Long courierId, Long orderId) {
        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);

            if (courierOpt.isEmpty()) {
                return;
            }

            Courier c = courierOpt.get();
            if (Objects.equals(c.getCurrentOrderId(), orderId)) {
                c.setStatus(CourierStatus.PICKED_UP);
                courierRepository.update(c);

                if (orderOpt.isPresent()) {
                    Order o = orderOpt.get();
                    o.setStatus(OrderStatus.PICKED_UP);
                    orderRepository.update(o);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении заказа", e);
        }
    }

    @Override
    public void completeOrder(Long courierId, Long orderId) {
        try {
            Optional<Courier> courierOpt = courierRepository.findById(courierId);
            Optional<Order> orderOpt = orderRepository.findById(orderId);

            if (courierOpt.isEmpty()) {
                return;
            }

            Courier c = courierOpt.get();
            if (Objects.equals(c.getCurrentOrderId(), orderId)) {
                c.setStatus(CourierStatus.ON_SHIFT);
                c.setCurrentOrderId(null);
                c.setCurrentBalance(c.getCurrentBalance() + 100.0);
                courierRepository.update(c);

                if (orderOpt.isPresent()) {
                    Order o = orderOpt.get();
                    o.setStatus(OrderStatus.COMPLETED);
                    orderRepository.update(o);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при завершении заказа", e);
        }
    }

    @Override
    public List<Order> getOrderHistory(Long courierId) {
        try {
            return orderRepository.findByCourierId(courierId).stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error("Ошибка при получении истории заказов", e);
            return List.of();
        }
    }

    @Override
    public Courier getCourierById(Long courierId) {
        try {
            return courierRepository.findById(courierId).orElse(null);
        } catch (SQLException e) {
            logger.error("Ошибка при получении курьера", e);
            return null;
        }
    }
}
