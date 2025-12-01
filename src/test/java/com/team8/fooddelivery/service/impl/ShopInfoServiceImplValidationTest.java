package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.shop.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShopInfoServiceImplValidationTest {

    private ShopInfoServiceImpl shopInfoService;

    @BeforeEach
    void setUp() {
        shopInfoService = new ShopInfoServiceImpl();
    }

    @Test
    void testRegisterShop_NullShop() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.registerShop(null, "test@test.com", "Password123!", "+79991234567");
        });
    }

    @Test
    void testRegisterShop_EmptyEmail() {
        Shop shop = new Shop();
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.registerShop(shop, "", "Password123!", "+79991234567");
        });
    }

    @Test
    void testRegisterShop_InvalidEmail() {
        Shop shop = new Shop();
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.registerShop(shop, "invalid-email", "Password123!", "+79991234567");
        });
    }

    @Test
    void testRegisterShop_InvalidPhone() {
        Shop shop = new Shop();
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.registerShop(shop, "test@test.com", "Password123!", "invalid");
        });
    }

    @Test
    void testRegisterShop_InvalidPassword() {
        Shop shop = new Shop();
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.registerShop(shop, "test@test.com", "weak", "+79991234567");
        });
    }

    @Test
    void testChangePassword_EmptyNewPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changePassword(1L, "test@test.com", "+79991234567", "", "Password123!");
        });
    }

    @Test
    void testChangePassword_InvalidNewPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changePassword(1L, "test@test.com", "+79991234567", "weak", "Password123!");
        });
    }

    @Test
    void testChangeEmailForAuth_EmptyNewEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changeEmailForAuth(1L, "+79991234567", "Password123!", "");
        });
    }

    @Test
    void testChangeEmailForAuth_InvalidNewEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changeEmailForAuth(1L, "+79991234567", "Password123!", "invalid-email");
        });
    }

    @Test
    void testChangePhoneForAuth_EmptyNewPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            shopInfoService.changePhoneForAuth(1L, "test@test.com", "Password123!", "");
        });
    }
}

