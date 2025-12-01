package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourierServiceImplValidationTest {

    private CourierServiceImpl courierService;

    @BeforeEach
    void setUp() {
        courierService = new CourierServiceImpl();
    }

    @Test
    void testRegisterNewCourier_InvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            courierService.registerNewCourier("Test", "invalid", "Password123!", "bike");
        });
    }

    @Test
    void testRegisterNewCourier_InvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            courierService.registerNewCourier("Test", "+79991234567", "weak", "bike");
        });
    }

    @Test
    void testUpdateCourierData_InvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            courierService.updateCourierData(1L, "Test", "invalid", "Password123!", "bike");
        });
    }

    @Test
    void testUpdateCourierData_InvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            courierService.updateCourierData(1L, "Test", "+79991234567", "weak", "bike");
        });
    }
}

