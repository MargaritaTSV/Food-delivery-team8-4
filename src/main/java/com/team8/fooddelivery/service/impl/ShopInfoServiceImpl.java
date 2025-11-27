package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.model.shop.ShopStatus;
import com.team8.fooddelivery.repository.ShopRepository;
import com.team8.fooddelivery.service.ShopInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

import static com.team8.fooddelivery.util.ValidationUtils.*;

public class ShopInfoServiceImpl implements ShopInfoService {

  private static final Logger logger = LoggerFactory.getLogger(ShopInfoServiceImpl.class);
  private final ShopRepository shopRepository = new ShopRepository();

  @Override
  public Shop registerShop(Shop infoAbout, String emailForAuth, String password, String phoneForAuth) {
    if (infoAbout == null || emailForAuth == null || emailForAuth.trim().isEmpty() ||
        password == null || password.trim().isEmpty() || phoneForAuth == null || phoneForAuth.trim().isEmpty()) {
      throw new IllegalArgumentException("Все поля должны быть заполнены");
    }

    if (!isValidEmail(emailForAuth)) {
      throw new IllegalArgumentException("Некорректный формат email");
    }

    if (!isValidPhone(phoneForAuth)) {
      throw new IllegalArgumentException("Некорректный формат телефона");
    }

    if (!isValidPassword(password)) {
      throw new IllegalArgumentException("Некорректный формат пароля");
    }

    try {
      if (shopRepository.findByEmailForAuth(emailForAuth).isPresent()) {
        throw new IllegalArgumentException("Email уже используется");
      }

      if (shopRepository.findByPhoneForAuth(phoneForAuth).isPresent()) {
        throw new IllegalArgumentException("Телефон уже используется");
      }

      infoAbout.setStatus(ShopStatus.PENDING);
      infoAbout.setEmailForAuth(emailForAuth);
      infoAbout.setPhoneForAuth(phoneForAuth);
      infoAbout.setPassword(password);

      Long shopId = shopRepository.save(infoAbout);
      infoAbout.setShopId(shopId);

      logger.info("Магазин зарегистрирован с ID: {}. Ожидает подтверждения.", shopId);
      return infoAbout;
    } catch (SQLException e) {
      logger.error("Ошибка при регистрации магазина", e);
      throw new RuntimeException("Не удалось зарегистрировать магазин", e);
    }
  }

  @Override
  public boolean approveShop(Long shopId) {
    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isEmpty()) {
        logger.warn("Магазин с ID {} не найден", shopId);
        return false;
      }

      Shop shop = shopOpt.get();
      if (shop.getStatus() != ShopStatus.PENDING) {
        logger.warn("Магазин с ID {} уже был обработан. Текущий статус: {}", shopId, shop.getStatus());
        return false;
      }

      shop.setStatus(ShopStatus.APPROVED);
      shopRepository.update(shop);
      logger.info("Магазин с ID {} одобрен", shopId);
      return true;
    } catch (SQLException e) {
      logger.error("Ошибка при одобрении магазина", e);
      return false;
    }
  }

  @Override
  public boolean rejectShop(String rejectionReason, Long shopId) {
    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isEmpty()) {
        logger.warn("Магазин с ID {} не найден", shopId);
        return false;
      }

      Shop shop = shopOpt.get();
      if (shop.getStatus() != ShopStatus.PENDING) {
        logger.warn("Магазин с ID {} уже был обработан. Текущий статус: {}", shopId, shop.getStatus());
        return false;
      }

      shop.setStatus(ShopStatus.REJECTED);
      shopRepository.update(shop);
      logger.info("Магазин с ID {} отклонен. Причина: {}", shopId, rejectionReason);
      return true;
    } catch (SQLException e) {
      logger.error("Ошибка при отклонении магазина", e);
      return false;
    }
  }

  @Override
  public String changePassword(Long shopId, String emailForAuth, String phoneForAuth, String newPassword, String password) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("Новый пароль не может быть пустым");
    }

    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Пароль не может быть пустым");
    }

    if (!isValidPassword(newPassword)) {
      throw new IllegalArgumentException("Новый пароль не корректный");
    }

    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isEmpty()) {
        throw new IllegalArgumentException("Магазин с ID " + shopId + " не найден");
      }

      Shop shop = shopOpt.get();
      if (!shop.getEmailForAuth().equals(emailForAuth) || !shop.getPhoneForAuth().equals(phoneForAuth)) {
        throw new SecurityException("Неверные аутентификационные данные");
      }

      if (password.equals(shop.getPassword())) {
        shop.setPassword(newPassword);
        shopRepository.update(shop);
        logger.info("Пароль для магазина с ID {} успешно изменен", shopId);
      }
      return "Пароль успешно изменен";
    } catch (SQLException e) {
      logger.error("Ошибка при смене пароля магазина", e);
      throw new RuntimeException("Не удалось изменить пароль", e);
    }
  }

  @Override
  public String changeEmailForAuth(Long shopId, String phoneForAuth, String password, String newEmailForAuth) {
    if (newEmailForAuth == null || newEmailForAuth.trim().isEmpty()) {
      throw new IllegalArgumentException("Новый email не может быть пустым");
    }

    if (!isValidEmail(newEmailForAuth)) {
      throw new IllegalArgumentException("Некорректный формат нового email");
    }

    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isEmpty()) {
        throw new IllegalArgumentException("Магазин с ID " + shopId + " не найден");
      }

      Shop shop = shopOpt.get();
      if (!shop.getPhoneForAuth().equals(phoneForAuth)) {
        throw new SecurityException("Неверный телефон для аутентификации");
      }

      if (!shop.getPassword().equals(password)) {
        throw new SecurityException("Неверный пароль");
      }

      if (shopRepository.findByEmailForAuth(newEmailForAuth).isPresent()) {
        throw new IllegalArgumentException("Новый email уже используется другим магазином");
      }

      String oldEmail = shop.getEmailForAuth();
      shop.setEmailForAuth(newEmailForAuth);
      shopRepository.update(shop);

      logger.info("Email для магазина с ID {} изменен с {} на {}", shopId, oldEmail, newEmailForAuth);
      return "Email для аутентификации успешно изменен";
    } catch (SQLException e) {
      logger.error("Ошибка при смене email магазина", e);
      throw new RuntimeException("Не удалось изменить email", e);
    }
  }

  @Override
  public String changePhoneForAuth(Long shopId, String emailForAuth, String password, String newPhoneForAuth) {
    if (newPhoneForAuth == null || newPhoneForAuth.trim().isEmpty()) {
      throw new IllegalArgumentException("Новый телефон не может быть пустым");
    }

    try {
      Optional<Shop> shopOpt = shopRepository.findById(shopId);
      if (shopOpt.isEmpty()) {
        throw new IllegalArgumentException("Магазин с ID " + shopId + " не найден");
      }

      Shop shop = shopOpt.get();
      if (!shop.getEmailForAuth().equals(emailForAuth)) {
        throw new SecurityException("Неверный email для аутентификации");
      }

      if (!shop.getPassword().equals(password)) {
        throw new SecurityException("Неверный пароль");
      }

      if (shopRepository.findByPhoneForAuth(newPhoneForAuth).isPresent()) {
        throw new IllegalArgumentException("Новый телефон уже используется другим магазином");
      }

      shop.setPhoneForAuth(newPhoneForAuth);
      shopRepository.update(shop);

      logger.info("Телефон для магазина с ID {} изменен", shopId);
      return "Телефон для аутентификации успешно изменен";
    } catch (SQLException e) {
      logger.error("Ошибка при смене телефона магазина", e);
      throw new RuntimeException("Не удалось изменить телефон", e);
    }
  }
}