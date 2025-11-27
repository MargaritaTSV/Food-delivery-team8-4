package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.AuthResponse;
import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.repository.ClientRepository;
import com.team8.fooddelivery.service.ClientService;
import com.team8.fooddelivery.util.PasswordUtils;
import com.team8.fooddelivery.util.ValidationUtils;
import com.team8.fooddelivery.util.JWTUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClientServiceImpl implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
    NotificationServiceImpl notificationService = new NotificationServiceImpl();

    private final ClientRepository clientRepository;
    private final CartServiceImpl cartService;

    public ClientServiceImpl(CartServiceImpl cartService) {
        this.cartService = cartService;
        this.clientRepository = new ClientRepository();
    }


    // =====================
    // Регистрация
    // =====================
    public Client register(String phone, String password, String name, String email, Address address) {
        logger.info("Попытка регистрации пользователя с телефоном: {}", phone);

        // Проверка телефона как уникального логина
        if (!ValidationUtils.isValidPhone(phone)) {
            logger.warn("Регистрация не удалась: неверный формат телефона {}", phone);
            throw new IllegalArgumentException("Неверный формат телефона");
        }
        if (isPhoneExists(phone)) {
            logger.warn("Регистрация не удалась: телефон {} уже существует", phone);
            throw new IllegalArgumentException("Пользователь с таким телефоном уже существует");
        }

        // Email можно менять, но он обязателен
        if (email == null || email.isBlank() || !ValidationUtils.isValidEmail(email)) {
            logger.warn("Регистрация не удалась: некорректный email {}", email);
            throw new IllegalArgumentException("Email некорректный");
        }

        if (password == null || password.isBlank()) {
            logger.warn("Регистрация не удалась: пустой пароль для {}", email);
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        if (!ValidationUtils.isValidPassword(password)) {
            logger.warn("Слабый пароль для {}", email);
            throw new IllegalArgumentException("Слабый пароль");
        }

        if (!ValidationUtils.isValidAddress(address)) {
            logger.warn("Неверный адрес для {}", email);
            throw new IllegalArgumentException("Неверный формат адреса");
        }



        String hashedPassword = PasswordUtils.hashPassword(password);

        Cart cart = cartService.createCartForClient(null); // Временная корзина, id будет установлен после сохранения клиента

        Client client = Client.builder()
                .name(name)
                .phone(phone)
                .passwordHash(hashedPassword)
                .email(email)
                .address(address)
                .createdAt(Instant.now())
                .status(ClientStatus.ACTIVE)
                .isActive(true)
                .cart(cart)
                .orderHistory(new ArrayList<>())
                .build();

        try {
            Long id = clientRepository.save(client);
            client.setId(id);
            cart.setClientId(id);
            logger.info("Пользователь зарегистрирован: {} — {}, id={}", name, phone, id);
            return client;
        } catch (SQLException e) {
            logger.error("Ошибка при сохранении клиента", e);
            throw new RuntimeException("Не удалось зарегистрировать клиента", e);
        }
    }
    // =====================
// Проверка логина и пароля (аутентификация)
// =====================
    public boolean authenticate(String login, String password) {
        try {
            Client client = null;
            // Пытаемся найти по телефону или email
            Optional<Client> byPhone = clientRepository.findByPhone(login);
            if (byPhone.isPresent()) {
                client = byPhone.get();
            } else {
                Optional<Client> byEmail = clientRepository.findByEmail(login);
                if (byEmail.isPresent()) {
                    client = byEmail.get();
                }
            }

            if (client == null || !client.isActive()) {
                logger.warn("Клиент {} не найден или деактивирован", login);
                return false;
            }

            boolean ok = PasswordUtils.checkPassword(password, client.getPasswordHash());
            if (ok) logger.info("Аутентификация успешна для {}", login);
            else logger.warn("Неверный пароль для {}", login);

            return ok;
        } catch (SQLException e) {
            logger.error("Ошибка при аутентификации", e);
            return false;
        }
    }

    // =====================
// Авторизация: вход в систему, выдача JWT, статус AUTHORIZED
// =====================
    public AuthResponse login(String login, String password) {
        if (!authenticate(login, password)) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        try {
            Client client = null;
            Optional<Client> byPhone = clientRepository.findByPhone(login);
            if (byPhone.isPresent()) {
                client = byPhone.get();
            } else {
                Optional<Client> byEmail = clientRepository.findByEmail(login);
                if (byEmail.isPresent()) {
                    client = byEmail.get();
                }
            }

            if (client == null) {
                throw new IllegalArgumentException("Клиент не найден");
            }

            String authToken = JWTUtil.generateToken(client.getId());

            // Меняем статус на AUTHORIZED
            client.setStatus(ClientStatus.AUTHORIZED);
            clientRepository.update(client);

            logger.info("Клиент {} авторизован, токен: {}", login, authToken);

            return new AuthResponse(client.getId(), authToken, client.getStatus());
        } catch (SQLException e) {
            logger.error("Ошибка при авторизации", e);
            throw new RuntimeException("Не удалось авторизовать клиента", e);
        }
    }


    // =====================
    // Обновление клиента
    // =====================
    @Override
    public Client update(Long clientId, String name, String email, Address address) {
        logger.info("Обновление данных клиента id={}", clientId);

        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                logger.warn("Клиент id={} не найден", clientId);
                throw new IllegalArgumentException("Клиент не найден");
            }

            Client client = clientOpt.get();

            if (Objects.equals(client.getName(), name)
                    && Objects.equals(client.getEmail(), email)
                    && Objects.equals(client.getAddress(), address)) {

                logger.warn("Изменений не обнаружено для id={}", clientId);
                throw new IllegalArgumentException("Изменений нет");
            }

            // Email можно менять
            if (email != null && !email.equals(client.getEmail())) {
                if (!ValidationUtils.isValidEmail(email)) {
                    logger.warn("Некорректный email {}", email);
                    throw new IllegalArgumentException("Неверный email");
                }
                if (isEmailExists(email)) {
                    logger.warn("Email {} уже используется", email);
                    throw new IllegalArgumentException("Email уже используется другим пользователем");
                }
                client.setEmail(email);
                logger.info("Email обновлен для id={} → {}", clientId, email);
            }

            if (address != null && !address.equals(client.getAddress())) {
                if (!ValidationUtils.isValidAddress(address)) {
                    logger.warn("Некорректный адрес для id={}", clientId);
                    throw new IllegalArgumentException("Неверный адрес");
                }
                client.setAddress(address);
                logger.info("Адрес обновлён для id={}", clientId);
            }

            if (name != null && !name.equals(client.getName())) {
                client.setName(name);
                logger.info("Имя обновлено id={} → {}", clientId, name);
            }

            client.setStatus(ClientStatus.UPDATED);
            clientRepository.update(client);
            logger.info("Обновление клиента id={} завершено успешно", clientId);
            return client;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении клиента", e);
            throw new RuntimeException("Не удалось обновить клиента", e);
        }
    }

    // =====================
    // Поиск клиента по телефону
    // =====================
    public Client getByPhone(String phone) {
        try {
            return clientRepository.findByPhone(phone).orElse(null);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске клиента по телефону", e);
            return null;
        }
    }

    // =====================
    // Поиск клиента по email
    // =====================
    public Client getByEmail(String email) {
        try {
            return clientRepository.findByEmail(email).orElse(null);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске клиента по email", e);
            return null;
        }
    }

    // =====================
    // Смена пароля
    // =====================
    public boolean changePassword(Long clientId, String oldPassword, String newPassword) {
        logger.info("попытка смены пароля id={}", clientId);

        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                logger.warn("Смена пароля не удалась: Клиент id={} не найден", clientId);
                throw new IllegalArgumentException("Клиент не найден");
            }

            Client client = clientOpt.get();

            if (!client.isActive()) {
                logger.warn("Смена пароля не удалась: Клиент id={} деактивирован", clientId);
                throw new IllegalStateException("Клиент деактивирован");
            }

            if (oldPassword == null || newPassword == null
                    || oldPassword.isBlank() || newPassword.isBlank()) {
                logger.warn("Смена пароля не удалась: Пустые пароли при смене id={}", clientId);
                throw new IllegalArgumentException("Пароли не могут быть пустыми");
            }

            if (!PasswordUtils.checkPassword(oldPassword, client.getPasswordHash())) {
                logger.warn("Смена пароля не удалась: Неверный старый пароль id={}", clientId);
                throw new IllegalArgumentException("Неверный старый пароль");
            }

            // Проверка сложности нового пароля
            if (!ValidationUtils.isValidPassword(newPassword)) {
                logger.warn("Смена пароля не удалась: Новый пароль слабый id={}", clientId);
                throw new IllegalArgumentException("Новый пароль не проходит валидацию");
            }

            // Хэширование и установка нового пароля
            String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
            client.setPasswordHash(hashedNewPassword);
            client.setStatus(ClientStatus.UPDATED_PASSWORD);
            clientRepository.update(client);
            logger.info("Пароль успешно изменён для клиента id=" + clientId);

            return true;
        } catch (SQLException e) {
            logger.error("Ошибка при смене пароля", e);
            throw new RuntimeException("Не удалось изменить пароль", e);
        }
    }

    // =====================
    // Активация/деактивация
    // =====================
    @Override
    public boolean deactivate(Long clientId) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                logger.warn("Деактивация: клиент id={} не найден", clientId);
                return false;
            }
            Client client = clientOpt.get();
            client.setActive(false);
            client.setStatus(ClientStatus.INACTIVE);
            clientRepository.update(client);
            logger.info("Клиент деактивирован id={}", clientId);
            return true;
        } catch (SQLException e) {
            logger.error("Ошибка при деактивации клиента", e);
            return false;
        }
    }

    @Override
    public boolean activate(Long clientId) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                logger.warn("Активация: клиент id={} не найден", clientId);
                return false;
            }
            Client client = clientOpt.get();
            client.setActive(true);
            client.setStatus(ClientStatus.ACTIVE);
            clientRepository.update(client);
            logger.info("Клиент активирован id={}", clientId);
            return true;
        } catch (SQLException e) {
            logger.error("Ошибка при активации клиента", e);
            return false;
        }
    }

    // =====================
    // Получение клиентов
    // =====================
    @Override
    public Client getById(Long clientId) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isPresent()) {
                logger.info("Получение клиента id={}", clientId);
                return clientOpt.get();
            } else {
                logger.warn("Клиент id={} не найден", clientId);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении клиента", e);
            return null;
        }
    }

    @Override
    public List<Client> listAll() {
        try {
            List<Client> clients = clientRepository.findAll();
            logger.info("Получение всех клиентов. total={}", clients.size());
            return clients;
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка клиентов", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getOrderHistory(Long clientId) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                logger.warn("История заказов пустая id={}", clientId);
                return new ArrayList<>();
            }
            Client client = clientOpt.get();
            List<String> history = client.getOrderHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            logger.info("История заказов id={}, entries={}", clientId, history.size());
            return new ArrayList<>(history);
        } catch (SQLException e) {
            logger.error("Ошибка при получении истории заказов", e);
            return new ArrayList<>();
        }
    }


    // =====================
    // Добавление записи в историю заказов
    // =====================
    public void addOrderHistoryEntry(Long clientId, String entry) {
        logger.info("Добавление записи в историю id={}, entry={}", clientId, entry);

        try {
            Optional<Client> clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty() || !clientOpt.get().isActive()) {
                logger.warn("Добавление записи не удалось: id={} отсутствует или деактивирован", clientId);
                throw new IllegalStateException("Клиент не найден или деактивирован");
            }

            Client client = clientOpt.get();
            List<String> history = client.getOrderHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            history.add("[" + timestamp + "] " + entry);
            client.setOrderHistory(history);
            clientRepository.update(client);

            logger.info("Запись добавлена id={}", clientId);
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении записи в историю", e);
            throw new RuntimeException("Не удалось добавить запись в историю", e);
        }
    }

    // =====================
    // Проверка уникальности
    // =====================
    private boolean isEmailExists(String email) {
        try {
            boolean exists = clientRepository.findByEmail(email).isPresent();
            logger.info("Проверка email '{}': {}", email, exists ? "занят" : "свободен");
            return exists;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке email", e);
            return false;
        }
    }

    private boolean isPhoneExists(String phone) {
        try {
            boolean existing = clientRepository.findByPhone(phone).isPresent();
            logger.info("Проверка телефона '{}': {}", phone, existing ? "занят" : "свободен");
            return existing;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке телефона", e);
            return false;
        }
    }

}
