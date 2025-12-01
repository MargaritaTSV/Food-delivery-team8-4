package com.team8.fooddelivery.model.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientStatusTest {

    @Test
    void testEnumValues() {
        ClientStatus[] values = ClientStatus.values();
        assertEquals(5, values.length);
        assertTrue(containsValue(values, ClientStatus.ACTIVE));
        assertTrue(containsValue(values, ClientStatus.INACTIVE));
        assertTrue(containsValue(values, ClientStatus.UPDATED));
        assertTrue(containsValue(values, ClientStatus.AUTHORIZED));
        assertTrue(containsValue(values, ClientStatus.UPDATED_PASSWORD));
    }

    @Test
    void testValueOf() {
        assertEquals(ClientStatus.ACTIVE, ClientStatus.valueOf("ACTIVE"));
        assertEquals(ClientStatus.INACTIVE, ClientStatus.valueOf("INACTIVE"));
        assertEquals(ClientStatus.UPDATED, ClientStatus.valueOf("UPDATED"));
        assertEquals(ClientStatus.AUTHORIZED, ClientStatus.valueOf("AUTHORIZED"));
        assertEquals(ClientStatus.UPDATED_PASSWORD, ClientStatus.valueOf("UPDATED_PASSWORD"));
    }

    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ClientStatus.valueOf("INVALID"));
    }

    private boolean containsValue(ClientStatus[] values, ClientStatus status) {
        for (ClientStatus value : values) {
            if (value == status) {
                return true;
            }
        }
        return false;
    }
}

