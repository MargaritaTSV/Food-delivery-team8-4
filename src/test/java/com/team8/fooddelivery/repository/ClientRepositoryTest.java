package com.team8.fooddelivery.repository;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Тесты ClientRepository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientRepositoryTest {

    private static ClientRepository clientRepository;
    private static Long testClientId;
    private static String createdPhone;
    private static String createdEmail;
    private static String createdName;

    @BeforeAll
    static void setUp() throws SQLException {
        // Настройка подключения
        String dbUrl = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/food_delivery");
        String dbUser = System.getProperty("db.user", "fooddelivery_user");
        String dbPassword = System.getProperty("db.password", "fooddelivery_pass");
        DatabaseConnection.setConnectionParams(dbUrl, dbUser, dbPassword);

        // Проверка подключения
        if (!DatabaseConnection.testConnection()) {
            throw new RuntimeException("Не удалось подключиться к базе данных. Убедитесь, что PostgreSQL запущен и БД создана.");
        }

        clientRepository = new ClientRepository();
    }

    @BeforeEach
    void setUpEach() throws SQLException {
        // Очистка тестовых данных перед каждым тестом (опционально)
        // Можно добавить очистку, если нужно
    }

    @Test
    @Order(1)
    @DisplayName("Сохранение клиента в БД")
    void testSaveClient() throws SQLException {
        Address address = Address.builder()
                .country("Россия")
                .city("Москва")
                .street("Ленина")
                .building("10")
                .apartment("5")
                .latitude(55.7558)
                .longitude(37.6173)
                .build();

        long suffix = System.currentTimeMillis();

        createdName = "Тестовый Клиент " + suffix;
        createdPhone = "+7999" + (suffix % 1_000_0000);
        createdEmail = "test" + suffix + "@example.com";

        Client client = Client.builder()
                .name(createdName)
                .phone(createdPhone)
                .email(createdEmail)
                .passwordHash("hashed_password")
                .address(address)
                .status(ClientStatus.ACTIVE)
                .isActive(true)
                .createdAt(Instant.now())
                .orderHistory(List.of())
                .build();

        testClientId = clientRepository.save(client);
        assertNotNull(testClientId, "ID клиента должен быть создан");
        assertTrue(testClientId > 0, "ID должен быть положительным числом");
    }

    @Test
    @Order(2)
    @DisplayName("Поиск клиента по ID")
    void testFindById() throws SQLException {
        assumeTrue(testClientId != null, "Тест сохранения должен пройти первым");

        Optional<Client> clientOpt = clientRepository.findById(testClientId);
        assertTrue(clientOpt.isPresent(), "Клиент должен быть найден");

        Client client = clientOpt.get();
        assertEquals(createdName, client.getName());
        assertEquals(createdPhone, client.getPhone());
        assertEquals(createdEmail, client.getEmail());
        assertEquals(ClientStatus.ACTIVE, client.getStatus());
    }

    @Test
    @Order(3)
    @DisplayName("Поиск клиента по телефону")
    void testFindByPhone() throws SQLException {
        assumeTrue(testClientId != null, "Тест сохранения должен пройти первым");

        Optional<Client> clientOpt = clientRepository.findByPhone(createdPhone);
        assertTrue(clientOpt.isPresent(), "Клиент должен быть найден по телефону");
        assertEquals(testClientId, clientOpt.get().getId());
    }

    @Test
    @Order(4)
    @DisplayName("Поиск клиента по email")
    void testFindByEmail() throws SQLException {
        assumeTrue(testClientId != null, "Тест сохранения должен пройти первым");

        Optional<Client> clientOpt = clientRepository.findByEmail(createdEmail);
        assertTrue(clientOpt.isPresent(), "Клиент должен быть найден по email");
        assertEquals(testClientId, clientOpt.get().getId());
    }

    @Test
    @Order(5)
    @DisplayName("Обновление клиента")
    void testUpdateClient() throws SQLException {
        assumeTrue(testClientId != null, "Тест сохранения должен пройти первым");

        Optional<Client> clientOpt = clientRepository.findById(testClientId);
        assertTrue(clientOpt.isPresent());

        Client client = clientOpt.get();
        client.setName("Обновленное Имя");
        client.setStatus(ClientStatus.UPDATED);

        clientRepository.update(client);

        Optional<Client> updatedClientOpt = clientRepository.findById(testClientId);
        assertTrue(updatedClientOpt.isPresent());
        assertEquals("Обновленное Имя", updatedClientOpt.get().getName());
        assertEquals(ClientStatus.UPDATED, updatedClientOpt.get().getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("Получение всех клиентов")
    void testFindAll() throws SQLException {
        List<Client> clients = clientRepository.findAll();
        assertNotNull(clients, "Список клиентов не должен быть null");
        assertFalse(clients.isEmpty(), "Должен быть хотя бы один клиент (тестовый)");
    }

    @Test
    @Order(7)
    @DisplayName("Клиент остается в базе после тестов")
    void testClientPersistence() throws SQLException {
        assumeTrue(testClientId != null, "Тест сохранения должен пройти первым");

        Optional<Client> clientOpt = clientRepository.findById(testClientId);
        assertTrue(clientOpt.isPresent(), "Клиент должен остаться в базе для последующих проверок");
    }
}

