package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.shop.Shop;

import java.util.List;
import java.util.Optional;

public interface ShopInfoService {

  Shop registerShop(Shop infoAbout, String emailForAuth, String password, String phoneForAuth); // + набор полей

  boolean approveShop(Long shopId); // возвращать статус

  boolean rejectShop(String rejectionReason, Long shopId); // возвращать статус

  String changePassword(Long shopId, String emailForAuth, String phoneForAuth, String newPassword, String password); // + new password

  String changeEmailForAuth(Long shopId, String phoneForAuth, String password, String newEmailForAuth); // + new email

  String changePhoneForAuth(Long shopId, String emailForAuth, String password, String newPhoneForAuth); // + new phone

  List<Shop> getAllShops();

  Optional<Shop> getShopById(Long shopId);

}
