<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Заказы магазина - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
<nav class="navbar">
    <div class="container">
        <h1>Food Delivery - Магазин</h1>
        <ul>
            <li><a href="${pageContext.request.contextPath}/shop/dashboard">Панель</a></li>
            <li><a href="${pageContext.request.contextPath}/products/list">Товары</a></li>
            <li><a href="${pageContext.request.contextPath}/shop/orders">Заказы</a></li>
            <li><a href="${pageContext.request.contextPath}/auth/logout">Выход</a></li>
        </ul>
    </div>
</nav>

<main class="container">
    <div class="section-header">
        <div>
            <h1>Заказы ресторана</h1>
            <p class="muted">Меняйте статусы PREPARING, READY, PICKED_UP, REJECTED и связывайте курьеров.</p>
        </div>
        <form method="GET" class="inline-form" action="${pageContext.request.contextPath}/shop/orders">
            <label class="sr-only" for="date">Дата</label>
            <input type="date" id="date" name="date" value="${param.date}">
            <button type="submit" class="btn btn-secondary btn-small">Обновить</button>
        </form>
    </div>

    <div class="orders-list">
        <c:forEach var="order" items="${orders}">
            <div class="order-card">
                <h3>Заказ #${order.orderId}</h3>
                <p><strong>Клиент:</strong> ${order.clientName} (${order.clientPhone})</p>
                <p><strong>Адрес:</strong> ${order.toStreet}, ${order.toHouse}</p>
                <p><strong>Состав:</strong> ${order.summary}</p>
                <p><strong>Сумма:</strong> ${order.totalPrice} ₽</p>
                <p><strong>Текущий статус:</strong> ${order.status}</p>

                <form method="POST" action="${pageContext.request.contextPath}/orders/update-status" class="inline-form">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <select name="status" required>
                        <option value="PREPARING">PREPARING</option>
                        <option value="READY">READY</option>
                        <option value="PICKED_UP">PICKED_UP</option>
                        <option value="REJECTED">REJECTED</option>
                    </select>
                    <button type="submit" class="btn btn-primary btn-small">Сохранить</button>
                </form>

                <form method="POST" action="${pageContext.request.contextPath}/orders/cancel" class="inline-form">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn btn-danger btn-small">Отменить (нет ингредиентов)</button>
                </form>
            </div>
        </c:forEach>
    </div>
</main>
</body>
</html>
