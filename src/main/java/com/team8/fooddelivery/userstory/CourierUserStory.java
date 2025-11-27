package com.team8.fooddelivery.userstory;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.PaymentMethodForOrder;
import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.model.order.OrderStatus;
import com.team8.fooddelivery.repository.CourierRepository;
import com.team8.fooddelivery.repository.OrderRepository;
import com.team8.fooddelivery.service.impl.CourierServiceImpl;
import com.team8.fooddelivery.util.DatabaseInitializer;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Optional;

public class CourierUserStory {

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        CourierServiceImpl courierService = new CourierServiceImpl();
        CourierRepository courierRepository = new CourierRepository();
        OrderRepository orderRepository = new OrderRepository();

        System.out.println("=== СИСТЕМА ДОСТАВКИ: КУРЬЕР ===");

        String courierPhone = "+79997776655";
        Long courierId = null;
        try {
            Optional<Courier> existingCourier = courierRepository.findByPhoneNumber(courierPhone);
            if (existingCourier.isPresent()) {
                courierId = existingCourier.get().getId();
            } else {
                courierId = courierService.registerNewCourier(
                        "Ivan Tokaev",
                        courierPhone,
                        "Ivan123!",
                        "scooter"
                );
                System.out.println("Зарегистрирован новый курьер с ID: " + courierId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить или создать курьера", e);
        }

        Courier courier = courierService.login("+79997776655", "Ivan123!");
        if (courier != null) {
            System.out.println("Вход выполнен: " + courier.getName());
        } else {
            System.out.println("Ошибка входа");
            return;
        }

        courierService.startShift(courierId);
        System.out.println("Курьер начал смену. Статус: " + courier.getStatus());

        Order order = findFirstReadyOrder(orderRepository);
        if (order == null) {
            System.out.println("Нет заказов, доступных для выдачи курьеру");
            return;
        }

        try {
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
            orderRepository.update(order);
        } catch (SQLException e) {
            System.out.println("Не удалось подготовить заказ к выдаче: " + e.getMessage());
            return;
        }

        boolean accepted = courierService.acceptOrder(courierId, order.getId());
        if (accepted) {
            System.out.println("Заказ #" + order.getId() + " принят");
        } else {
            System.out.println("Не удалось принять заказ");
        }

        courierService.pickupOrder(courierId, order.getId());
        System.out.println("Заказ забран. Текущий статус: " + courierService.login(courierPhone, "Ivan123!").getStatus());

        courierService.completeOrder(courierId, order.getId());
        courier = courierService.login(courierPhone, "Ivan123!");
        System.out.println("Заказ доставлен. Статус: " + courier.getStatus());
        System.out.println("Баланс курьера: " + courier.getCurrentBalance());

        System.out.println("История заказов:");
        courierService.getOrderHistory(courierId)
                .forEach(historyOrder -> System.out.println(" - " + historyOrder));

        courierService.endShift(courierId);
        courier = courierService.login(courierPhone, "Ivan123!");
        System.out.println("Курьер закончил смену. Статус: " + courier.getStatus());
    }

    private static Order findFirstReadyOrder(OrderRepository orderRepository) {
        try {
            return orderRepository.findByStatus(OrderStatus.READY_FOR_PICKUP).stream()
                    .filter(o -> o.getCourierId() == null)
                    .min(Comparator.comparing(Order::getCreatedAt))
                    .orElseGet(() -> {
                        try {
                            return orderRepository.findByStatus(OrderStatus.CONFIRMED).stream()
                                    .filter(o -> o.getCourierId() == null)
                                    .min(Comparator.comparing(Order::getCreatedAt))
                                    .orElse(null);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (SQLException e) {
            System.out.println("Ошибка при поиске заказов для курьера: " + e.getMessage());
            return null;
        }
    }
}
