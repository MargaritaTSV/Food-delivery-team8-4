<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Доступные заказы - Food Delivery</title>
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
            </ul>
        </div>
    </nav>

    <main class="container">
        <h1>Доступные заказы</h1>
        <p>Выберите заказ, который готовы забрать и доставить.</p>

        <c:if test="${empty availableOrders}">
            <div class="alert alert-info">Нет доступных заказов</div>
        </c:if>

        <c:if test="${not empty availableOrders}">
            <div class="orders-list">
                <c:forEach var="order" items="${availableOrders}">
                    <div class="order-card">
                        <h3>Заказ #${order.id}</h3>
                        <p><strong>Откуда:</strong> ${order.fromStreet} ${order.fromHouse}</p>
                        <p><strong>Куда:</strong> ${order.toStreet} ${order.toHouse}</p>
                        <c:if test="${not empty order.comment}">
                            <p><strong>Комментарий:</strong> ${order.comment}</p>
                        </c:if>
                        <p><strong>Сумма:</strong> ${order.totalPrice} ₽</p>

                        <form method="POST" action="${pageContext.request.contextPath}/courier/accept-order">
                            <input type="hidden" name="orderId" value="${order.id}">
                            <button type="submit" class="btn btn-success btn-large">ПРИНЯТЬ ЗАКАЗ</button>
                        </form>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <a href="${pageContext.request.contextPath}/courier/dashboard" class="btn btn-secondary">Назад</a>
    </main>
</body>
</html>
