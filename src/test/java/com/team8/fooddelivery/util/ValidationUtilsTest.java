package com.team8.fooddelivery.util;

import com.team8.fooddelivery.model.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

  // ==========================================
  // EMAIL TEST
  // ==========================================
  @Test
  @DisplayName("Email: Валидные и невалидные сценарии")
  void testIsValidEmail() {
    assertFalse(ValidationUtils.isValidEmail(null));
    assertFalse(ValidationUtils.isValidEmail(""));
    assertFalse(ValidationUtils.isValidEmail("   ")); // Blank
    assertFalse(ValidationUtils.isValidEmail("plainaddress"));
    assertFalse(ValidationUtils.isValidEmail("@example.com")); // No local part
    assertFalse(ValidationUtils.isValidEmail("email@")); // No domain
    assertFalse(ValidationUtils.isValidEmail("email@example")); // No TLD
    assertTrue(ValidationUtils.isValidEmail("email@example.com"));
    assertTrue(ValidationUtils.isValidEmail("test.email+tag@example.co.uk"));
    assertTrue(ValidationUtils.isValidEmail("user123@test-domain.com"));
  }

  // ==========================================
  // PHONE TEST
  // ==========================================
  @Test
  @DisplayName("Phone: Валидные и невалидные сценарии")
  void testIsValidPhone() {
    assertFalse(ValidationUtils.isValidPhone(null));
    assertFalse(ValidationUtils.isValidPhone("123"));
    assertFalse(ValidationUtils.isValidPhone(""));
    assertFalse(ValidationUtils.isValidPhone("79991234567")); // Missing + or 8
    assertFalse(ValidationUtils.isValidPhone("+7999123456")); // Too short
    assertFalse(ValidationUtils.isValidPhone("+799912345678")); // Too long
    assertTrue(ValidationUtils.isValidPhone("+79991234567"));
    assertTrue(ValidationUtils.isValidPhone("89991234567")); // 8 prefix
  }

  // ==========================================
  // PASSWORD TEST
  // ==========================================
  @Test
  @DisplayName("Password: Проверка сложности")
  void testIsValidPassword() {
    assertFalse(ValidationUtils.isValidPassword(null));
    assertFalse(ValidationUtils.isValidPassword(""));
    assertFalse(ValidationUtils.isValidPassword("weak")); // No uppercase, digit, special
    assertFalse(ValidationUtils.isValidPassword("Weak1")); // No special char
    assertFalse(ValidationUtils.isValidPassword("Weak!")); // No digit
    assertFalse(ValidationUtils.isValidPassword("weak1!")); // No uppercase
    assertFalse(ValidationUtils.isValidPassword("Weak1!")); // Too short (7 chars)
    assertTrue(ValidationUtils.isValidPassword("Strong1!Pass"));
    assertTrue(ValidationUtils.isValidPassword("A1!bcdefg")); // Minimum valid
  }

  // ==========================================
  // ADDRESS TEST
  // ==========================================
  @Test
  @DisplayName("Address: Проверка всех полей по очереди")
  void testIsValidAddress() {
    // 1. Null адрес
    assertFalse(ValidationUtils.isValidAddress(null));

    // Создаем эталонный валидный адрес
    // Обрати внимание: floor - это Integer, а latitude - Double
    Address validAddr = Address.builder()
        .country("Russia")
        .city("Moscow")
        .street("Lenina")
        .building("1")
        .apartment("10")
        .entrance("1")
        .floor(5)
        .latitude(55.0)
        .longitude(37.0)
        .build();

    // 2. Успешный сценарий
    assertTrue(ValidationUtils.isValidAddress(validAddr));

    // 3. Проверяем каждое поле (создаем копию и ломаем одно поле)

    // Country
    Address badCountry = copy(validAddr); badCountry.setCountry(null);
    assertFalse(ValidationUtils.isValidAddress(badCountry));

    // City
    Address badCity = copy(validAddr); badCity.setCity("");
    assertFalse(ValidationUtils.isValidAddress(badCity));

    // Street
    Address badStreet = copy(validAddr); badStreet.setStreet(null);
    assertFalse(ValidationUtils.isValidAddress(badStreet));

    // Building
    Address badBuilding = copy(validAddr); badBuilding.setBuilding(null);
    assertFalse(ValidationUtils.isValidAddress(badBuilding));

    // Floor
    Address badFloor = copy(validAddr); badFloor.setFloor(null);
    assertFalse(ValidationUtils.isValidAddress(badFloor));

    // Apartment
    Address badApartment = copy(validAddr); badApartment.setApartment(null);
    assertFalse(ValidationUtils.isValidAddress(badApartment));

    // Entrance
    Address badEntrance = copy(validAddr); badEntrance.setEntrance(null);
    assertFalse(ValidationUtils.isValidAddress(badEntrance));

    // Coords (равны 0)
    Address badCoords = copy(validAddr); badCoords.setLatitude(0.0);
    assertFalse(ValidationUtils.isValidAddress(badCoords));

    Address badLongitude = copy(validAddr); badLongitude.setLongitude(0.0);
    assertFalse(ValidationUtils.isValidAddress(badLongitude));
  }

  @Test
  @DisplayName("Address: Попадание в блок catch (через NPE)")
  void testIsValidAddress_Exception() {
    /*
     * Вместо создания анонимного класса мы используем особенность Java.
     * Если latitude == null, то попытка сравнить его с 0 вызовет NullPointerException.
     * ValidationUtils поймает этот Exception и вернет false.
     * Это покроет блок catch.
     */

    Address riskyAddress = Address.builder()
        .country("Russia")
        .city("Moscow")
        .street("Lenina")
        .building("1")
        .apartment("10")
        .entrance("1")
        .floor(5)
        .latitude(null) // <--- ЭТО ВЫЗОВЕТ ОШИБКУ ВНУТРИ ВАЛИДАТОРА
        .longitude(37.0)
        .build();

    boolean result = ValidationUtils.isValidAddress(riskyAddress);

    assertFalse(result, "При возникновении исключения (NPE) должно возвращаться false");
  }

  // Вспомогательный метод
  private Address copy(Address original) {
    return Address.builder()
        .country(original.getCountry())
        .city(original.getCity())
        .street(original.getStreet())
        .building(original.getBuilding())
        .apartment(original.getApartment())
        .entrance(original.getEntrance())
        .floor(original.getFloor())
        .latitude(original.getLatitude())
        .longitude(original.getLongitude())
        .build();
  }
}