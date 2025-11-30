<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Личный кабинет курьера - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <nav class="navbar">
        <div class="container">
            <h1>Food Delivery Курьер</h1>
            <ul>
                <li><a href="${pageContext.request.contextPath}/courier/dashboard">Главная</a></li>
                <li><a href="${pageContext.request.contextPath}/courier/orders">Заказы</a></li>
                <li><a href="${pageContext.request.contextPath}/courier/history">История</a></li>
                <li><a href="${pageContext.request.contextPath}/auth/logout">Выход</a></li>
            </ul>
        </div>
    </nav>

    <main class="container">
        <h1>Личный кабинет курьера</h1>

        <div class="courier-info">
            <h2>${courier.name}</h2>
            <p>Статус: <span class="status-${courier.status}">${courier.status}</span></p>
            <div class="info-grid">
                <div><strong>Телефон:</strong> ${courier.phoneNumber}</div>
                <div><strong>Транспорт:</strong> ${courier.transportType}</div>
                <div><strong>Баланс смены:</strong> ${courier.currentBalance} ₽</div>
                <div><strong>Текущий заказ:</strong> <c:out value="${courier.currentOrderId == 0 ? 'нет' : courier.currentOrderId}"/></div>
                <div><strong>Последний адрес:</strong> ${courier.lastAddress}</div>
            </div>
        </div>

        <div class="courier-actions">
            <c:choose>
                <c:when test="${courier.status == 'OFFLINE' || courier.status == 'offline'}">
                    <form method="POST" action="${pageContext.request.contextPath}/courier/start-shift" class="inline-form">
                        <label for="lastAddress">Адрес начала смены</label>
                        <input type="text" id="lastAddress" name="lastAddress" value="${courier.lastAddress}" placeholder="Укажите, где вы находитесь" required>
                        <button type="submit" class="btn btn-success btn-large">НАЧАТЬ СМЕНУ</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <form method="POST" action="${pageContext.request.contextPath}/courier/end-shift" class="inline-form">
                        <button type="submit" class="btn btn-danger btn-large">ЗАВЕРШИТЬ СМЕНУ</button>
                    </form>

                    <a href="${pageContext.request.contextPath}/courier/orders" class="btn btn-primary btn-large">Доступные заказы</a>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${not empty activeOrder}">
            <section class="section">
                <h2>Активный заказ #${activeOrder.id}</h2>
                <p><strong>Откуда:</strong> ${activeOrder.fromStreet} ${activeOrder.fromHouse}</p>
                <p><strong>Куда:</strong> ${activeOrder.toStreet} ${activeOrder.toHouse}</p>
                <p><strong>Контакт клиента:</strong> ${activeOrder.clientPhone}</p>

                <div class="courier-actions">
                    <form method="POST" action="${pageContext.request.contextPath}/order/pickup">
                        <input type="hidden" name="orderId" value="${activeOrder.id}">
                        <button type="submit" class="btn btn-secondary">ЗАКАЗ ЗАБРАЛ</button>
                    </form>

                    <form method="POST" action="${pageContext.request.contextPath}/order/complete">
                        <input type="hidden" name="orderId" value="${activeOrder.id}">
                        <button type="submit" class="btn btn-success">ПЕРЕДАЛ ЗАКАЗ</button>
                    </form>

                    <a class="btn btn-primary" href="tel:${activeOrder.clientPhone}">ПОЗВОНИТЬ КЛИЕНТУ</a>
                    <a class="btn btn-danger" href="${pageContext.request.contextPath}/courier/problem?orderId=${activeOrder.id}">ПРОБЛЕМА С ЗАКАЗОМ</a>
                </div>
            </section>
        </c:if>

        <section class="section">
            <h2>Быстрые действия</h2>
            <div class="courier-actions">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/courier/orders">Посмотреть доступные заказы</a>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/courier/history">История за день</a>
            </div>
        </section>
    </main>
</body>
</html>
