package com.team8.fooddelivery.model.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodForOrderTest {

    @Test
    void testEnumValues() {
        PaymentMethodForOrder[] values = PaymentMethodForOrder.values();
        assertEquals(3, values.length);
        assertTrue(containsValue(values, PaymentMethodForOrder.CARD));
        assertTrue(containsValue(values, PaymentMethodForOrder.CASH));
        assertTrue(containsValue(values, PaymentMethodForOrder.ONLINE));
    }

    @Test
    void testValueOf() {
        assertEquals(PaymentMethodForOrder.CARD, PaymentMethodForOrder.valueOf("CARD"));
        assertEquals(PaymentMethodForOrder.CASH, PaymentMethodForOrder.valueOf("CASH"));
        assertEquals(PaymentMethodForOrder.ONLINE, PaymentMethodForOrder.valueOf("ONLINE"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethodForOrder.valueOf("INVALID"));
    }

    private boolean containsValue(PaymentMethodForOrder[] values, PaymentMethodForOrder method) {
        for (PaymentMethodForOrder value : values) {
            if (value == method) {
                return true;
            }
        }
        return false;
    }
}

