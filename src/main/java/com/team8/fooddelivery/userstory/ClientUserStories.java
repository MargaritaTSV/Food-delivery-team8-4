package com.team8.fooddelivery.userstory;

import com.team8.fooddelivery.model.AuthResponse;
import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.service.impl.CartServiceImpl;
import com.team8.fooddelivery.service.impl.ClientServiceImpl;
import com.team8.fooddelivery.util.JWTUtil;
import com.team8.fooddelivery.util.DatabaseInitializer;

public class ClientUserStories {

    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        CartServiceImpl cartService = new CartServiceImpl();
        ClientServiceImpl clientService = new ClientServiceImpl(cartService);

        // =====================
        // 1. Регистрация клиентов
        // =====================
        System.out.println("\n1. Регистрация клиентов\n");

        Address address1 = new Address(
                "Россия", "Москва", "Тверская", "1", "10", "1", 3,
                55.7558, 37.6173, "Квартира с видом на Кремль", "ЦАО"
        );

        Address address2 = new Address(
                "Россия", "Санкт-Петербург", "Невский", "10", "5", "2", 2,
                59.9311, 30.3609, "Рядом с метро Площадь Восстания", "Центральный"
        );

        Client c1 = clientService.getByPhone("89991112233");
        if (c1 == null) {
            c1 = clientService.register("89991112233", "Ivan123!", "Иван Иванов", "ivan@example.com", address1);
        }

        Client c2 = clientService.getByPhone("89995556677");
        if (c2 == null) {
            c2 = clientService.register("89995556677", "Maria456@", "Мария Петрова", "maria@example.com", address2);
        }

        System.out.println("Клиенты зарегистрированы:");
        clientService.listAll().forEach(System.out::println);

        // =====================
        // 2. Аутентификация и JWT авторизация
        // =====================
        System.out.println("\n2. Аутентификация и авторизация\n");

        String phone = "89991112233";
        String password = "Ivan123!";

        if (clientService.authenticate(phone, password)) {
            Client client = clientService.getByPhone(phone); // новый метод getByPhone
            client.setStatus(ClientStatus.AUTHORIZED);

            String token = JWTUtil.generateToken(client.getId());
            System.out.println("Иван авторизован:");
            System.out.println("ClientId: " + client.getId());
            System.out.println("Token: " + token);
            System.out.println("Status: " + client.getStatus());
        }
        // =====================
        // 3. Авторизация (JWT)
        // =====================
        System.out.println("\n3. Авторизация (JWT)\n");

        AuthResponse authC1 = clientService.login("89991112233", "Ivan123!");
        System.out.println("Иван авторизован:");
        System.out.println("ClientId: " + authC1.getClientId());
        System.out.println("Token: " + authC1.getAuthToken());
        System.out.println("Status: " + authC1.getStatus());

        // =====================
        // 4. Добавление истории заказов
        // =====================
        clientService.addOrderHistoryEntry(c1.getId(), "Заказ #1006: тушеная картошечка, 1250₽");
        clientService.addOrderHistoryEntry(c1.getId(), "Заказ #1001: суши, 1250₽");
        clientService.addOrderHistoryEntry(c1.getId(), "Заказ #1015: пицца, 780₽");

        System.out.println("\nИстория заказов клиента " + c1.getName() + ":");
        clientService.getOrderHistory(c1.getId()).forEach(System.out::println);

        // =====================
        // 5. Обновление клиента (email)
        // =====================
        clientService.update(c2.getId(), null, "masha@example.com", address1);
        Client updatedC2 = clientService.getById(c2.getId());
        updatedC2.setStatus(ClientStatus.UPDATED);
        System.out.println("\nПосле обновления email второго клиента:");
        System.out.println(updatedC2);

        // =====================
        // 6. Деактивация
        // =====================
        clientService.deactivate(c2.getId());
        System.out.println("\nПосле деактивации второго клиента:");
        System.out.println(clientService.getById(c2.getId()));

        // =====================
        // 7. Проверка входа после деактивации
        // =====================
        System.out.println("\nПопытка аутентификации деактивированного клиента:");
        System.out.println("Мария: " + clientService.authenticate("89995556677", "Maria456@")); // false

        // =====================
        // 8. Добавление заказа активного клиента
        // =====================
        clientService.addOrderHistoryEntry(c1.getId(), "Заказ #1018: роллы, 950₽");
        System.out.println("\nОбновлённая история заказов активного клиента " + c1.getName() + ":");
        clientService.getOrderHistory(c1.getId()).forEach(System.out::println);

        // =====================
        // 9. Активация клиента
        // =====================
        clientService.activate(c2.getId());
        System.out.println("\nПосле активации второго клиента:");
        System.out.println(clientService.getById(c2.getId()));

        AuthResponse authC2 = clientService.login("89995556677", "Maria456@");
        System.out.println("\nМария авторизована:");
        System.out.println("ClientId: " + authC2.getClientId());
        System.out.println("Token: " + authC2.getAuthToken());
        System.out.println("Status: " + authC2.getStatus());

        // =====================
        // 10. Смена пароля
        // =====================
        System.out.println("\nСмена пароля Иван:");
        clientService.changePassword(c1.getId(), "Ivan123!", "Ivan789@");

        System.out.println("Проверка аутентификации со старым и новым паролем:");
        System.out.println("Старый пароль: " + clientService.authenticate("89991112233", "Ivan123!")); // false
        System.out.println("Новый пароль: " + clientService.authenticate("89991112233", "Ivan789@"));  // true
    }
}
