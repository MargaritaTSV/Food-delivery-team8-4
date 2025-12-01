package com.team8.fooddelivery.model.order;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void testEnumValues() {
        OrderStatus[] values = OrderStatus.values();
        assertEquals(9, values.length);
        assertTrue(containsValue(values, OrderStatus.PENDING));
        assertTrue(containsValue(values, OrderStatus.PREPARING));
        assertTrue(containsValue(values, OrderStatus.CONFIRMED));
        assertTrue(containsValue(values, OrderStatus.PAID));
        assertTrue(containsValue(values, OrderStatus.READY_FOR_PICKUP));
        assertTrue(containsValue(values, OrderStatus.DELIVERING));
        assertTrue(containsValue(values, OrderStatus.PICKED_UP));
        assertTrue(containsValue(values, OrderStatus.COMPLETED));
        assertTrue(containsValue(values, OrderStatus.CANCELLED));
    }

    @Test
    void testValueOf() {
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.PREPARING, OrderStatus.valueOf("PREPARING"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.PAID, OrderStatus.valueOf("PAID"));
        assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.valueOf("READY_FOR_PICKUP"));
        assertEquals(OrderStatus.DELIVERING, OrderStatus.valueOf("DELIVERING"));
        assertEquals(OrderStatus.PICKED_UP, OrderStatus.valueOf("PICKED_UP"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.valueOf("COMPLETED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.valueOf("INVALID"));
    }

    private boolean containsValue(OrderStatus[] values, OrderStatus status) {
        for (OrderStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}

