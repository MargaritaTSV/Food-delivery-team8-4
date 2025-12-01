package com.team8.fooddelivery.model.courier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourierStatusTest {

    @Test
    void testEnumValues() {
        CourierStatus[] values = CourierStatus.values();
        assertEquals(4, values.length);
        assertTrue(containsValue(values, CourierStatus.OFF_SHIFT));
        assertTrue(containsValue(values, CourierStatus.ON_SHIFT));
        assertTrue(containsValue(values, CourierStatus.DELIVERING));
        assertTrue(containsValue(values, CourierStatus.PICKED_UP));
    }

    @Test
    void testValueOf() {
        assertEquals(CourierStatus.OFF_SHIFT, CourierStatus.valueOf("OFF_SHIFT"));
        assertEquals(CourierStatus.ON_SHIFT, CourierStatus.valueOf("ON_SHIFT"));
        assertEquals(CourierStatus.DELIVERING, CourierStatus.valueOf("DELIVERING"));
        assertEquals(CourierStatus.PICKED_UP, CourierStatus.valueOf("PICKED_UP"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> CourierStatus.valueOf("INVALID"));
    }

    private boolean containsValue(CourierStatus[] values, CourierStatus status) {
        for (CourierStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}

