package com.team8.fooddelivery.model.shop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShopStatusTest {

    @Test
    void testEnumValues() {
        ShopStatus[] values = ShopStatus.values();
        assertEquals(6, values.length);
        assertTrue(containsValue(values, ShopStatus.PENDING));
        assertTrue(containsValue(values, ShopStatus.ACTIVE));
        assertTrue(containsValue(values, ShopStatus.SUSPENDED));
        assertTrue(containsValue(values, ShopStatus.CLOSED));
        assertTrue(containsValue(values, ShopStatus.APPROVED));
        assertTrue(containsValue(values, ShopStatus.REJECTED));
    }

    @Test
    void testValueOf() {
        assertEquals(ShopStatus.PENDING, ShopStatus.valueOf("PENDING"));
        assertEquals(ShopStatus.ACTIVE, ShopStatus.valueOf("ACTIVE"));
        assertEquals(ShopStatus.SUSPENDED, ShopStatus.valueOf("SUSPENDED"));
        assertEquals(ShopStatus.CLOSED, ShopStatus.valueOf("CLOSED"));
        assertEquals(ShopStatus.APPROVED, ShopStatus.valueOf("APPROVED"));
        assertEquals(ShopStatus.REJECTED, ShopStatus.valueOf("REJECTED"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ShopStatus.valueOf("INVALID"));
    }

    private boolean containsValue(ShopStatus[] values, ShopStatus status) {
        for (ShopStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}

