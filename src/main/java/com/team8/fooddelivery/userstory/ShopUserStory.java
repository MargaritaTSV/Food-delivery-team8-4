package com.team8.fooddelivery.userstory;

import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.repository.ShopRepository;
import com.team8.fooddelivery.service.impl.ShopInfoServiceImpl;
import com.team8.fooddelivery.util.DatabaseInitializer;

import java.sql.SQLException;

public class ShopUserStory {
  public static void main(String[] args) {
    DatabaseInitializer.initializeDatabase();

    ShopInfoServiceImpl service = new ShopInfoServiceImpl();
    ShopRepository shopRepository = new ShopRepository();

    String phone = "+79123456789";
    String email = "Test@mail.com";

    Shop shop;
    try {
      shop = shopRepository.findByPhoneForAuth(phone).orElseGet(Shop::new);
    } catch (SQLException e) {
      throw new RuntimeException("Не удалось получить магазин", e);
    }

    if (shop.getShopId() == null) {
      shop.setNaming("Test Shop");
      shop.setPhoneForAuth(phone);
      Shop registeredShop = service.registerShop(shop, email, "Password123!", phone);
      shop = registeredShop;
      System.out.println("Магазин зарегистрирован: " + registeredShop.getShopId() + ", статус: " + registeredShop.getStatus());
    } else {
      System.out.println("Используем существующий магазин: " + shop.getShopId());
    }

    boolean approved = service.approveShop(shop.getShopId());
    System.out.println("Магазин одобрен: " + approved);

    System.out.println(shop);

    String passResult = service.changePassword(shop.getShopId(), email, phone, "NewPassword123!", "Password123!");
    System.out.println("Смена пароля: " + passResult);

    String emailResult = service.changeEmailForAuth(shop.getShopId(), phone, "NewPassword123!", "new@mail.com");
    System.out.println("Смена email: " + emailResult);

    String phoneResult = service.changePhoneForAuth(shop.getShopId(), "new@mail.com", "NewPassword123!", "+79099999999");
    System.out.println("Смена телефона: " + phoneResult);

  }
}
