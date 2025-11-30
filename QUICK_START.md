# ⚡ БЫСТРЫЙ СТАРТ (30 МИНУТ)

> Если у вас нет времени на полный анализ - начните отсюда!  
> Этот документ описывает 3 критических исправления, которые займут 30 минут.

---

## 🔴 ПРОБЛЕМА #1: Разные учётные данные БД (10 минут)

### Текущая ситуация:
```
Все тесты должны использовать единые данные: fooddelivery_user / fooddelivery_pass
```

### ДЕЙСТВИЕ: Создать `TestDatabaseConfig.java`

**Файл:** `src/test/java/com/team8/fooddelivery/util/TestDatabaseConfig.java`

```java
package com.team8.fooddelivery.util;

public class TestDatabaseConfig {
    public static final String DB_URL = System.getProperty("db.url",
            "jdbc:postgresql://localhost:5432/food_delivery");
    public static final String DB_USER = System.getProperty("db.user", "fooddelivery_user");
    public static final String DB_PASSWORD = System.getProperty("db.password", "fooddelivery_pass");

    public static void initialize() {
        DatabaseConnection.setConnectionParams(DB_URL, DB_USER, DB_PASSWORD);
        DatabaseConnection.initializeDatabase();
        if (!DatabaseConnection.testConnection()) {
            throw new RuntimeException("Не удалось подключиться к БД");
        }
    }
}
```

### Потом обновите все `@BeforeAll`:

**ДО:**
```java
@BeforeAll
static void setupDatabaseConnection() {
    String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
    String dbUser = System.getProperty("db.user", "fooddelivery_user");
    String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
    DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);
    // ...
}
```

**ПОСЛЕ:**
```java
@BeforeAll
static void setupDatabaseConnection() {
    TestDatabaseConfig.initialize();
}
```

✅ **РЕЗУЛЬТАТ:** Все тесты используют одну конфигурацию!

---

## 🔴 ПРОБЛЕМА #2: Зависимости между тестами (10 минут)

### Текущая ситуация:
```
@Test @Order(1) testSaveClient()
    ↓ создаёт testClientId
@Test @Order(2) testFindById()
    ↓ зависит от testClientId
```

### ДЕЙСТВИЕ: Переписать тесты БЕЗ @Order

**ДО (НЕПРАВИЛЬНО):**
```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientRepositoryTest {
    private static Long testClientId;  // ← ОБЩЕЕ СОСТОЯНИЕ!

    @Test
    @Order(1)
    void testSaveClient() throws SQLException {
        // ...
        testClientId = clientRepository.save(client);
    }

    @Test
    @Order(2)
    void testFindById() throws SQLException {
        assumeTrue(testClientId != null);  // ← ЗАВИСИТ ОТ ПЕРВОГО!
        // ...
    }
}
```

**ПОСЛЕ (ПРАВИЛЬНО):**
```java
public class ClientRepositoryTest {
    // ❌ Нет @TestMethodOrder
    // ❌ Нет static testClientId
    
    @BeforeEach
    void setUp() throws SQLException {
        // Очистить БД перед каждым тестом
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM clients CASCADE");
        }
    }

    @Test
    void testSaveClient() throws SQLException {
        // ✅ Создаёт своего клиента
        Client client = Client.builder()
            .name("Test " + System.currentTimeMillis())
            .email("test" + System.currentTimeMillis() + "@example.com")
            .phone("+7999" + System.currentTimeMillis())
            // ...
            .build();
        
        Long clientId = clientRepository.save(client);
        assertNotNull(clientId);
    }

    @Test
    void testFindById() throws SQLException {
        // ✅ Создаёт своего клиента, не зависит от первого теста!
        Client client = Client.builder()
            .name("Test " + System.currentTimeMillis())
            .email("test" + System.currentTimeMillis() + "@example.com")
            .phone("+7999" + System.currentTimeMillis())
            // ...
            .build();
        
        Long clientId = clientRepository.save(client);
        Optional<Client> found = clientRepository.findById(clientId);
        assertTrue(found.isPresent());
    }
}
```

✅ **РЕЗУЛЬТАТ:** Каждый тест независимый, можно запустить в любом порядке!

---

## 🔴 ПРОБЛЕМА #3: БД не очищается между тестами (10 минут)

### Текущая ситуация:
```
Test 1: Создаёт client с email="test@example.com" ✓
Test 2: Пытается создать client с email="test@example.com" ✗ (UNIQUE constraint)
```

### ДЕЙСТВИЕ: Добавить очистку в @BeforeEach

**Вставить в КАЖДЫЙ тест class:**

```java
@BeforeEach
void setUp() throws SQLException {
    clearDatabase();
    // ... инициализировать repositories
}

private void clearDatabase() throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement()) {
        // Удалить данные (порядок важен!)
        stmt.executeUpdate("DELETE FROM carts CASCADE");
        stmt.executeUpdate("DELETE FROM orders CASCADE");
        stmt.executeUpdate("DELETE FROM clients CASCADE");
        stmt.executeUpdate("DELETE FROM products CASCADE");
        stmt.executeUpdate("DELETE FROM shops CASCADE");
        // ...
    }
}
```

✅ **РЕЗУЛЬТАТ:** БД чистая перед каждым тестом!

---

## 📋 ЧЕКЛИСТ (ВСЕ 3 ПРОБЛЕМЫ)

### Шаг 1: Создать TestDatabaseConfig.java
- [ ] Создать файл в `src/test/java/com/team8/fooddelivery/util/`
- [ ] Копировать код из раздела "ПРОБЛЕМА #1"
- [ ] Проверить что компилируется

### Шаг 2: Обновить @BeforeAll везде
- [ ] `SimpleConnectionTest.java` → использовать TestDatabaseConfig
- [ ] `DatabaseConnectionTest.java` → использовать TestDatabaseConfig
- [ ] `CartRepositoryTest.java` → использовать TestDatabaseConfig
- [ ] `ClientRepositoryTest.java` → использовать TestDatabaseConfig
- [ ] `ClientCartIntegrationTest.java` → использовать TestDatabaseConfig
- [ ] `OrderCourierIntegrationTest.java` → использовать TestDatabaseConfig
- [ ] `OrderInteractionIntegrationTest.java` → использовать TestDatabaseConfig
- [ ] `ShopProductIntegrationTest.java` → использовать TestDatabaseConfig

### Шаг 3: Удалить @Order и переписать тесты
- [ ] `CartRepositoryTest.java` → удалить @TestMethodOrder, переписать тесты
- [ ] `ClientRepositoryTest.java` → удалить @TestMethodOrder, переписать тесты

### Шаг 4: Добавить clearDatabase()
- [ ] `DatabaseConnectionTest.java`
- [ ] `CartRepositoryTest.java`
- [ ] `ClientRepositoryTest.java`
- [ ] `ClientCartIntegrationTest.java`
- [ ] `OrderCourierIntegrationTest.java`
- [ ] `OrderInteractionIntegrationTest.java`
- [ ] `ShopProductIntegrationTest.java`

### Шаг 5: Перезапустить тесты
- [ ] `mvn test` → Все должны пройти ✅
- [ ] Запустить каждый тест отдельно → Все должны пройти ✅

---

## 🧪 БЫСТРАЯ ПРОВЕРКА

После исправлений выполните эти команды:

```bash
# Полный тест suite
mvn test

# Тест каждого класса отдельно
mvn test -Dtest=ClientRepositoryTest
mvn test -Dtest=CartRepositoryTest
mvn test -Dtest=ClientCartIntegrationTest
mvn test -Dtest=OrderCourierIntegrationTest
mvn test -Dtest=OrderInteractionIntegrationTest
mvn test -Dtest=ShopProductIntegrationTest

# Запустить дважды подряд (должно работать оба раза)
mvn test && mvn test
```

**ОЖИДАЕМЫЙ РЕЗУЛЬТАТ:** ✅ Все тесты проходят ВО ВСЕХ СЛУЧАЯХ

---

## 📊 ДО И ПОСЛЕ

| Метрика | ДО | ПОСЛЕ |
|---------|----|----|
| Все тесты проходят | ❌ Нет | ✅ Да |
| Можно запустить отдельный тест | ❌ Нет | ✅ Да |
| Тесты падают с constraint violations | ❌ Да | ✅ Нет |
| Зависимость между тестами | ❌ Высокая | ✅ Нет |
| ОЦЕНКА | **4.5/10** ⚠️ | **7/10** ✅ |

---

## ⏱️ ВРЕМЯ ВЫПОЛНЕНИЯ

- **Проблема #1 (конфиг):** 10 минут
- **Проблема #2(зависимости):** 10 минут
- **Проблема #3 (очистка):** 10 минут
- **Проверка:** 5 минут

**ИТОГО:** ~30-40 минут

---

## 📚 ДОПОЛНИТЕЛЬНО

Если хотите полный анализ - смотрите:
- [ANALYSIS_INDEX.md](ANALYSIS_INDEX.md) — Индекс всех документов
- [code_analysis_report.md](code_analysis_report.md) — Полный анализ всех 10 проблем
- [TEST_FIXES_README.md](TEST_FIXES_README.md) — Пошаговые инструкции

---

## ❓ ЧАСТЫЕ ВОПРОСЫ

**В: Почему эти исправления критичные?**  
О: Потому что без них:
- Тесты падают непредсказуемо (flaky)
- Нельзя запустить отдельный тест
- CI/CD pipeline не может быть надёжным

**В: А другие проблемы тоже нужно исправлять?**  
О: Да, но эти 3 - самые критичные. После этого можно улучшать логирование, убирать copy-paste, и т.д.

**В: Сколько других проблем?**  
О: Всего 10 проблем. 3 из них критичные (исправляем сейчас), 3 высокоприоритетные, 4 средней важности.

**В: Нужно ли менять основной код?**  
О: Нет! Только тестовый код.

---

🚀 **ГОТОВО К ДЕЙСТВИЮ!**

Скопируй код выше, примени 3 исправления, и тесты будут работать!

---

**Дата:** 30 ноября 2025  
**Время:** ~30 минут  
**Сложность:** ⭐⭐ (средняя)

