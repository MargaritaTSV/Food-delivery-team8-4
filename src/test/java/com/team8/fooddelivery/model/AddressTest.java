package com.team8.fooddelivery.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void testBuilder() {
        Address address = Address.builder()
                .country("Россия")
                .city("Москва")
                .street("Ленина")
                .building("10")
                .apartment("5")
                .entrance("1")
                .floor(3)
                .latitude(55.7558)
                .longitude(37.6173)
                .addressNote("Код домофона: 1234")
                .district("Центральный")
                .build();

        assertEquals("Россия", address.getCountry());
        assertEquals("Москва", address.getCity());
        assertEquals("Ленина", address.getStreet());
        assertEquals("10", address.getBuilding());
        assertEquals("5", address.getApartment());
        assertEquals("1", address.getEntrance());
        assertEquals(3, address.getFloor());
        assertEquals(55.7558, address.getLatitude());
        assertEquals(37.6173, address.getLongitude());
        assertEquals("Код домофона: 1234", address.getAddressNote());
        assertEquals("Центральный", address.getDistrict());
    }

    @Test
    void testAllArgsConstructor() {
        Address address = new Address(
                "Россия", "Санкт-Петербург", "Невский проспект", "20",
                "10", "2", 5, 59.9343, 30.3351, "Вход со двора", "Центральный"
        );

        assertEquals("Россия", address.getCountry());
        assertEquals("Санкт-Петербург", address.getCity());
        assertEquals("Невский проспект", address.getStreet());
        assertEquals("20", address.getBuilding());
        assertEquals("10", address.getApartment());
        assertEquals("2", address.getEntrance());
        assertEquals(5, address.getFloor());
        assertEquals(59.9343, address.getLatitude());
        assertEquals(30.3351, address.getLongitude());
        assertEquals("Вход со двора", address.getAddressNote());
        assertEquals("Центральный", address.getDistrict());
    }

    @Test
    void testEqualsAndHashCode() {
        Address address1 = Address.builder()
                .country("Россия")
                .city("Москва")
                .street("Ленина")
                .building("10")
                .build();

        Address address2 = Address.builder()
                .country("Россия")
                .city("Москва")
                .street("Ленина")
                .building("10")
                .build();

        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void testToString() {
        Address address = Address.builder()
                .country("Россия")
                .city("Москва")
                .street("Ленина")
                .building("10")
                .build();

        assertNotNull(address.toString());
        assertTrue(address.toString().contains("Москва"));
    }
}

