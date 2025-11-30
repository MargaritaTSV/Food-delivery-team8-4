# 🛠️ РУКОВОДСТВО ПО РЕАЛИЗАЦИИ FRONTEND НА SERVLET

> Food Delivery - Пошаговое руководство  
> Дата: 30 ноября 2025

---

## 📋 СОДЕРЖАНИЕ

1. [Подготовка проекта](#подготовка-проекта)
2. [Структура каталогов](#структура-каталогов)
3. [Конфигурация web.xml](#конфигурация-webxml)
4. [Примеры Servlet](#примеры-servlet)
5. [Примеры JSP](#примеры-jsp)
6. [Вспомогательные классы](#вспомогательные-классы)
7. [Фильтры](#фильтры)
8. [Тестирование](#тестирование)

---

## 🔧 ПОДГОТОВКА ПРОЕКТА

### Шаг 1: Обновить pom.xml

**Добавить зависимости:**

```xml
<!-- Servlet API (Jakarta) -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>

<!-- JSP/JSTL -->
<dependency>
    <groupId>jakarta.servlet.jsp</groupId>
    <artifactId>jakarta.servlet.jsp-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>3.0.0</version>
</dependency>

<!-- JSON Parsing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- Tomcat для разработки -->
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-core</artifactId>
    <version>10.1.11</version>
    <scope>provided</scope>
</dependency>
```

### Шаг 2: Собираем исполняемый JAR с embedded Tomcat

**В pom.xml должно быть:**

```xml
<packaging>jar</packaging>

<!-- tomcat-embed-core + tomcat-embed-jasper без scope provided -->
```

---

## 📁 СТРУКТУРА КАТАЛОГОВ

**Создать:**

```
src/main/webapp/
├── WEB-INF/
│   ├── web.xml
│   └── jsp/
│       ├── layout/
│       │   ├── header.jsp
│       │   ├── footer.jsp
│       │   ├── navbar.jsp
│       │   └── sidebar.jsp
│       ├── auth/
│       │   ├── login.jsp
│       │   ├── register.jsp
│       │   └── forgot-password.jsp
│       ├── client/
│       │   ├── home.jsp
│       │   ├── profile.jsp
│       │   ├── my-orders.jsp
│       │   └── addresses.jsp
│       ├── shop/
│       │   ├── list.jsp
│       │   └── details.jsp
│       ├── cart/
│       │   ├── view.jsp
│       │   └── checkout.jsp
│       ├── order/
│       │   ├── confirmation.jsp
│       │   └── track.jsp
│       ├── courier/
│       │   ├── dashboard.jsp
│       │   ├── orders.jsp
│       │   └── deliveries.jsp
│       └── error/
│           ├── 404.jsp
│           ├── 500.jsp
│           └── error.jsp
└── resources/
    ├── css/
    │   ├── style.css
    │   ├── responsive.css
    │   └── theme.css
    ├── js/
    │   ├── main.js
    │   ├── cart.js
    │   ├── validation.js
    │   └── utils.js
    ├── images/
    │   ├── logo.png
    │   ├── icons/
    │   └── backgrounds/
    └── fonts/
        └── ...
```

---

## ⚙️ КОНФИГУРАЦИЯ web.xml

**Файл:** `src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <!-- Приложение -->
    <display-name>Food Delivery</display-name>
    <description>Food Delivery Application (Team 8)</description>

    <!-- SESSION -->
    <session-config>
        <cookie-config>
            <http-only>true</http-only>
            <secure>false</secure>
        </cookie-config>
        <tracking-mode>COOKIE</tracking-mode>
        <timeout>30</timeout>
    </session-config>

    <!-- ФИЛЬТРЫ -->
    <filter>
        <filter-name>AuthenticationFilter</filter-name>
        <filter-class>com.team8.fooddelivery.filter.AuthenticationFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>AuthenticationFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>AuthorizationFilter</filter-name>
        <filter-class>com.team8.fooddelivery.filter.AuthorizationFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>AuthorizationFilter</filter-name>
        <url-pattern>/client/*</url-pattern>
        <url-pattern>/courier/*</url-pattern>
    </filter-mapping>

    <filter>
        <filter-name>EncodingFilter</filter-name>
        <filter-class>com.team8.fooddelivery.filter.EncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>EncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!-- SERVLETS -->
    <!-- Auth Servlets -->
    <servlet>
        <servlet-name>LoginServlet</servlet-name>
        <servlet-class>com.team8.fooddelivery.servlet.auth.LoginServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>LoginServlet</servlet-name>
        <url-pattern>/login</url-pattern>
    </servlet-mapping>

    <servlet>
        <servlet-name>AuthServlet</servlet-name>
        <servlet-class>com.team8.fooddelivery.servlet.auth.AuthServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>AuthServlet</servlet-name>
        <url-pattern>/auth/*</url-pattern>
    </servlet-mapping>

    <!-- Client Servlets -->
    <servlet>
        <servlet-name>ClientHomeServlet</servlet-name>
        <servlet-class>com.team8.fooddelivery.servlet.client.ClientHomeServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>ClientHomeServlet</servlet-name>
        <url-pattern>/client/home</url-pattern>
    </servlet-mapping>

    <servlet>
        <servlet-name>ShopListServlet</servlet-name>
        <servlet-class>com.team8.fooddelivery.servlet.shop.ShopListServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>ShopListServlet</servlet-name>
        <url-pattern>/shop/list</url-pattern>
    </servlet-mapping>

    <!-- Cart Servlets -->
    <servlet>
        <servlet-name>CartServlet</servlet-name>
        <servlet-class>com.team8.fooddelivery.servlet.cart.CartServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>CartServlet</servlet-name>
        <url-pattern>/cart/*</url-pattern>
    </servlet-mapping>

    <!-- Error Handler -->
    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/jsp/error/404.jsp</location>
    </error-page>

    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/jsp/error/500.jsp</location>
    </error-page>

    <!-- MIME Types -->
    <mime-mapping>
        <extension>json</extension>
        <mime-type>application/json</mime-type>
    </mime-mapping>

    <!-- Добро пожаловать -->
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>

</web-app>
```

---

## 🔵 ПРИМЕРЫ SERVLET

### Пример 1: AuthServlet (Аутентификация)

**Файл:** `src/main/java/com/team8/fooddelivery/servlet/auth/AuthServlet.java`

```java
package com.team8.fooddelivery.servlet.auth;

import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.client.ClientStatus;
import com.team8.fooddelivery.service.ClientService;
import com.team8.fooddelivery.service.impl.ClientServiceImpl;
import com.team8.fooddelivery.util.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AuthServlet.class);
    private final ClientService clientService = new ClientServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else if ("/register".equals(pathInfo)) {
            handleRegister(request, response);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        } else {
            response.sendError(404);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            // Валидация
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("error", "Email и пароль обязательны");
                request.getRequestDispatcher("/login").forward(request, response);
                return;
            }
            
            // Проверка в БД
            Optional<Client> clientOpt = clientService.authenticate(email, password);
            
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                
                if (client.getStatus() != ClientStatus.ACTIVE) {
                    request.setAttribute("error", "Аккаунт неактивен");
                    request.getRequestDispatcher("/login").forward(request, response);
                    return;
                }
                
                // Создать сессию
                SessionManager.createSession(request.getSession(), client);
                log.info("User {} logged in", email);
                
                response.sendRedirect(request.getContextPath() + "/client/home");
            } else {
                request.setAttribute("error", "Неверный email или пароль");
                request.getRequestDispatcher("/login").forward(request, response);
            }
        } catch (Exception e) {
            log.error("Login error", e);
            request.setAttribute("error", "Ошибка при входе: " + e.getMessage());
            request.getRequestDispatcher("/login").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        try {
            // Валидация
            if (name == null || name.isEmpty() || 
                email == null || email.isEmpty() || 
                phone == null || phone.isEmpty() || 
                password == null || password.isEmpty()) {
                
                request.setAttribute("error", "Все поля обязательны");
                request.getRequestDispatcher("/register").forward(request, response);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                request.setAttribute("error", "Пароли не совпадают");
                request.getRequestDispatcher("/register").forward(request, response);
                return;
            }
            
            // Проверка что email уникален
            Optional<Client> existing = clientService.findByEmail(email);
            if (existing.isPresent()) {
                request.setAttribute("error", "Email уже используется");
                request.getRequestDispatcher("/register").forward(request, response);
                return;
            }
            
            // Создать клиента
            Client client = Client.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .passwordHash(password) // В реальности нужно хэшировать!
                .status(ClientStatus.ACTIVE)
                .isActive(true)
                .build();
            
            Long clientId = clientService.save(client);
            
            log.info("New user registered: {}", email);
            
            // Автоматически залогинить
            SessionManager.createSession(request.getSession(), client);
            response.sendRedirect(request.getContextPath() + "/client/home");
            
        } catch (Exception e) {
            log.error("Registration error", e);
            request.setAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            request.getRequestDispatcher("/register").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        SessionManager.invalidateSession(request.getSession());
        log.info("User logged out");
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
```

---

### Пример 2: ClientHomeServlet

**Файл:** `src/main/java/com/team8/fooddelivery/servlet/client/ClientHomeServlet.java`

```java
package com.team8.fooddelivery.servlet.client;

import com.team8.fooddelivery.model.shop.Shop;
import com.team8.fooddelivery.service.ShopInfoService;
import com.team8.fooddelivery.service.impl.ShopInfoServiceImpl;
import com.team8.fooddelivery.util.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet("/client/home")
public class ClientHomeServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ClientHomeServlet.class);
    private final ShopInfoService shopInfoService = new ShopInfoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Получить пользователя из сессии
            Long userId = (Long) request.getSession().getAttribute("userId");
            
            // Получить все магазины
            List<Shop> shops = shopInfoService.getAllShops();
            
            request.setAttribute("shops", shops);
            request.setAttribute("userId", userId);
            
            log.debug("Home page loaded for user: {}", userId);
            
            request.getRequestDispatcher("/WEB-INF/jsp/client/home.jsp")
                .forward(request, response);
            
        } catch (Exception e) {
            log.error("Error loading home page", e);
            request.setAttribute("error", "Ошибка загрузки страницы");
            response.sendError(500);
        }
    }
}
```

---

### Пример 3: CartServlet (JSON для AJAX)

**Файл:** `src/main/java/com/team8/fooddelivery/servlet/cart/CartServlet.java`

```java
package com.team8.fooddelivery.servlet.cart;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.team8.fooddelivery.service.CartService;
import com.team8.fooddelivery.service.impl.CartServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/cart/*")
public class CartServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(CartServlet.class);
    private final CartService cartService = new CartServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        
        try {
            if ("/add".equals(pathInfo)) {
                handleAddToCart(request, response, writer);
            } else if ("/remove".equals(pathInfo)) {
                handleRemoveFromCart(request, response, writer);
            } else if ("/update".equals(pathInfo)) {
                handleUpdateCart(request, response, writer);
            } else {
                sendError(response, writer, "Unknown action");
            }
        } catch (Exception e) {
            log.error("Cart error", e);
            sendError(response, writer, e.getMessage());
        }
    }

    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, PrintWriter writer)
            throws IOException {
        
        Long clientId = (Long) request.getSession().getAttribute("userId");
        Long productId = Long.parseLong(request.getParameter("productId"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        
        try {
            Long cartId = cartService.addToCart(clientId, productId, quantity);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("cartId", cartId);
            result.addProperty("message", "Товар добавлен в корзину");
            
            writer.print(result.toString());
            
        } catch (Exception e) {
            sendError(response, writer, "Ошибка при добавлении в корзину");
        }
    }

    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, PrintWriter writer)
            throws IOException {
        
        Long cartItemId = Long.parseLong(request.getParameter("cartItemId"));
        
        try {
            cartService.removeFromCart(cartItemId);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Товар удалён из корзины");
            
            writer.print(result.toString());
            
        } catch (Exception e) {
            sendError(response, writer, "Ошибка при удалении");
        }
    }

    private void handleUpdateCart(HttpServletRequest request, HttpServletResponse response, PrintWriter writer)
            throws IOException {
        
        Long cartItemId = Long.parseLong(request.getParameter("cartItemId"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        
        try {
            cartService.updateQuantity(cartItemId, quantity);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Количество обновлено");
            
            writer.print(result.toString());
            
        } catch (Exception e) {
            sendError(response, writer, "Ошибка при обновлении");
        }
    }

    private void sendError(HttpServletResponse response, PrintWriter writer, String message) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("error", message);
        writer.print(error.toString());
    }
}
```

---

## 📄 ПРИМЕРЫ JSP

### Пример 1: login.jsp

**Файл:** `src/main/webapp/WEB-INF/jsp/auth/login.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Вход - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-box">
            <h1>Food Delivery</h1>
            <h2>Вход</h2>
            
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    ${error}
                </div>
            </c:if>
            
            <form method="POST" action="${pageContext.request.contextPath}/auth/login">
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" required 
                           placeholder="your@email.com">
                </div>
                
                <div class="form-group">
                    <label for="password">Пароль:</label>
                    <input type="password" id="password" name="password" required>
                </div>
                
                <button type="submit" class="btn btn-primary btn-block">
                    Войти
                </button>
            </form>
            
            <p class="auth-link">
                Нет аккаунта? <a href="${pageContext.request.contextPath}/register">Зарегистрироваться</a>
            </p>
        </div>
    </div>
    <script src="${pageContext.request.contextPath}/resources/js/main.js"></script>
</body>
</html>
```

---

### Пример 2: home.jsp (Главная страница клиента)

**Файл:** `src/main/webapp/WEB-INF/jsp/client/home.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Food Delivery - Главная</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/navbar.jsp" %>
    
    <main class="container">
        <div class="page-header">
            <h1>Доставка еды</h1>
            <input type="search" id="search" placeholder="Поиск магазина...">
        </div>
        
        <div class="shops-grid">
            <c:forEach var="shop" items="${shops}">
                <div class="shop-card">
                    <img src="${pageContext.request.contextPath}/resources/images/shop-${shop.id}.png" 
                         alt="${shop.naming}">
                    <h3>${shop.naming}</h3>
                    <p class="rating">★ ${shop.rating}</p>
                    <p class="delivery">Доставка: 30-40 мин</p>
                    <a href="${pageContext.request.contextPath}/shop/details?id=${shop.id}" 
                       class="btn btn-small">
                        Открыть
                    </a>
                </div>
            </c:forEach>
        </div>
    </main>
    
    <%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
    
    <script src="${pageContext.request.contextPath}/resources/js/main.js"></script>
</body>
</html>
```

---

## 🛠️ ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ

### SessionManager

**Файл:** `src/main/java/com/team8/fooddelivery/util/SessionManager.java`

```java
package com.team8.fooddelivery.util;

import com.team8.fooddelivery.model.client.Client;
import com.team8.fooddelivery.model.courier.Courier;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final long SESSION_TIMEOUT = 30 * 60; // 30 минут в секундах
    
    public static void createSession(HttpSession session, Client client) {
        session.setAttribute("userId", client.getId());
        session.setAttribute("userRole", "CLIENT");
        session.setAttribute("userName", client.getName());
        session.setAttribute("userEmail", client.getEmail());
        session.setMaxInactiveInterval((int) SESSION_TIMEOUT);
        log.debug("Created client session for user: {}", client.getId());
    }
    
    public static void createSession(HttpSession session, Courier courier) {
        session.setAttribute("userId", courier.getId());
        session.setAttribute("userRole", "COURIER");
        session.setAttribute("userName", courier.getName());
        session.setMaxInactiveInterval((int) SESSION_TIMEOUT);
        log.debug("Created courier session for user: {}", courier.getId());
    }
    
    public static void invalidateSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        session.invalidate();
        log.debug("Invalidated session for user: {}", userId);
    }
    
    public static boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }
    
    public static String getUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }
    
    public static Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
```

---

### RequestValidator

**Файл:** `src/main/java/com/team8/fooddelivery/util/RequestValidator.java`

```java
package com.team8.fooddelivery.util;

import java.util.regex.Pattern;

public class RequestValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{10,}$"
    );
    
    public static boolean validateEmail(String email) throws IllegalArgumentException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Неверный формат email");
        }
        return true;
    }
    
    public static boolean validatePassword(String password) throws IllegalArgumentException {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Пароль должен быть не менее 6 символов");
        }
        return true;
    }
    
    public static boolean validatePhone(String phone) throws IllegalArgumentException {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Телефон не может быть пустым");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Неверный формат телефона");
        }
        return true;
    }
    
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("[<>\"']", "");
    }
}
```

---

## 🔒 ФИЛЬТРЫ

### AuthenticationFilter

**Файл:** `src/main/java/com/team8/fooddelivery/filter/AuthenticationFilter.java`

```java
package com.team8.fooddelivery.filter;

import com.team8.fooddelivery.util.SessionManager;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    // Публичные страницы без аутентификации
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/login",
        "/register",
        "/resources",
        "/index.jsp"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Пропустить публичные пути
        boolean isPublic = PUBLIC_PATHS.stream()
            .anyMatch(path::startsWith);
        
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }
        
        // Проверить аутентификацию
        if (!SessionManager.isAuthenticated(httpRequest.getSession())) {
            log.warn("Unauthorized access attempt to: {}", path);
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

---

### AuthorizationFilter

**Файл:** `src/main/java/com/team8/fooddelivery/filter/AuthorizationFilter.java`

```java
package com.team8.fooddelivery.filter;

import com.team8.fooddelivery.util.SessionManager;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter({"/client/*", "/courier/*"})
public class AuthorizationFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String userRole = SessionManager.getUserRole(httpRequest.getSession());
        String requestURI = httpRequest.getRequestURI();
        
        // Проверить роль
        if (requestURI.contains("/client/") && !"CLIENT".equals(userRole)) {
            log.warn("Unauthorized role access attempt: {} trying to access {}", userRole, requestURI);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        if (requestURI.contains("/courier/") && !"COURIER".equals(userRole)) {
            log.warn("Unauthorized role access attempt: {} trying to access {}", userRole, requestURI);
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## ✅ ТЕСТИРОВАНИЕ

### Чек-лист Servlet тестирования

```
[ ] Запросы GET возвращают корректный статус 200
[ ] POST запросы обрабатываются правильно
[ ] Редиректы работают правильно
[ ] Ошибки возвращают правильные коды (404, 500)
[ ] Сессии создаются и удаляются
[ ] Аутентификация работает
[ ] Авторизация блокирует неавторизованный доступ
[ ] JSON ответы валидны
[ ] Параметры парсятся правильно
[ ] Исключения обрабатываются
```

---

**Версия:** 1.0  
**Дата:** 30 ноября 2025  
**Статус:** Готово для реализации

