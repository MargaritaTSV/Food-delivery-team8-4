<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Профиль - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/navbar.jsp" %>

    <main class="container">
        <h1>Мой профиль</h1>

        <c:if test="${not empty success}">
            <div class="alert alert-success">${success}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <section class="section">
            <h2>Данные аккаунта</h2>
            <div class="info-grid">
                <p><strong>ID:</strong> ${client.id}</p>
                <p><strong>Статус:</strong> ${client.status}</p>
                <p><strong>Создан:</strong> ${client.createdAt}</p>
                <p><strong>Активен:</strong> <c:out value="${client.active}"/></p>
            </div>
        </section>

        <section class="section">
            <h2>Контакты и адрес</h2>
            <form method="POST" action="${pageContext.request.contextPath}/client/update-profile">
                <div class="info-grid">
                    <div class="form-group">
                        <label>Имя:</label>
                        <input type="text" name="name" value="${client.name}" required>
                    </div>
                    <div class="form-group">
                        <label>Email:</label>
                        <input type="email" name="email" value="${client.email}" required>
                    </div>
                    <div class="form-group">
                        <label>Телефон:</label>
                        <input type="tel" name="phone" value="${client.phone}" pattern="89[0-9]{9}" placeholder="89XXXXXXXXX" required>
                    </div>
                    <div class="form-group">
                        <label>Страна:</label>
                        <input type="text" name="country" value="${client.address.country}">
                    </div>
                    <div class="form-group">
                        <label>Город:</label>
                        <input type="text" name="city" value="${client.address.city}">
                    </div>
                    <div class="form-group">
                        <label>Улица:</label>
                        <input type="text" name="street" value="${client.address.street}">
                    </div>
                    <div class="form-group">
                        <label>Здание:</label>
                        <input type="text" name="building" value="${client.address.building}">
                    </div>
                    <div class="form-group">
                        <label>Квартира:</label>
                        <input type="text" name="apartment" value="${client.address.apartment}">
                    </div>
                    <div class="form-group">
                        <label>Подъезд:</label>
                        <input type="text" name="entrance" value="${client.address.entrance}">
                    </div>
                    <div class="form-group">
                        <label>Этаж:</label>
                        <input type="text" name="floor" value="${client.address.floor}">
                    </div>
                    <div class="form-group">
                        <label>Широта:</label>
                        <input type="text" name="latitude" value="${client.address.latitude}">
                    </div>
                    <div class="form-group">
                        <label>Долгота:</label>
                        <input type="text" name="longitude" value="${client.address.longitude}">
                    </div>
                    <div class="form-group" style="grid-column: 1 / -1;">
                        <label>Комментарий к адресу:</label>
                        <textarea name="addressNote" rows="2">${client.address.addressNote}</textarea>
                    </div>
                </div>
                <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            </form>
        </section>

        <section class="section">
            <h2>Смена пароля</h2>
            <form method="POST" action="${pageContext.request.contextPath}/client/change-password">
                <div class="info-grid">
                    <div class="form-group">
                        <label>Текущий пароль:</label>
                        <input type="password" name="currentPassword" required>
                    </div>
                    <div class="form-group">
                        <label>Новый пароль:</label>
                        <input type="password" name="newPassword" required
                               pattern="(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-]).{8,}"
                               title="Минимум 8 символов, 1 цифра, 1 заглавная буква и 1 спецсимвол">
                    </div>
                    <div class="form-group">
                        <label>Повторите новый пароль:</label>
                        <input type="password" name="confirmPassword" required>
                    </div>
                </div>
                <button type="submit" class="btn btn-secondary">Обновить пароль</button>
            </form>
        </section>

        <section class="section">
            <h2>Уведомления</h2>
            <c:if test="${empty notifications}">
                <p>Новых уведомлений нет.</p>
            </c:if>
            <c:if test="${not empty notifications}">
                <ul>
                    <c:forEach var="note" items="${notifications}">
                        <li><strong>${note.notificationType}</strong>: ${note.message} (${note.timestamp})</li>
                    </c:forEach>
                </ul>
            </c:if>
        </section>

        <section class="section">
            <h2>Деактивировать аккаунт</h2>
            <p>После деактивации доступ к оформлению заказов и корзине будет закрыт.</p>
            <form method="POST" action="${pageContext.request.contextPath}/client/deactivate">
                <button type="submit" class="btn btn-danger">Деактивировать</button>
            </form>
        </section>
    </main>

    <%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
</body>
</html>
