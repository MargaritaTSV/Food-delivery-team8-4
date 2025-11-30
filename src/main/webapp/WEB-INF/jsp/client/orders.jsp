<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Мои заказы - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/navbar.jsp" %>

    <main class="container">
        <h1>Мои заказы</h1>

        <c:if test="${empty orders}">
            <p class="no-orders">У вас нет заказов</p>
            <a href="${pageContext.request.contextPath}/client/home" class="btn btn-primary">
                Начать покупки
            </a>
        </c:if>

        <c:if test="${not empty orders}">
            <table class="orders-table">
                <thead>
                    <tr>
                        <th>Заказ №</th>
                        <th>Дата</th>
                        <th>Сумма</th>
                        <th>Статус</th>
                        <th>Действие</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="order" items="${orders}">
                        <tr>
                            <td>${order.id}</td>
                            <td>${order.createdAt}</td>
                            <td>${order.totalPrice} ₽</td>
                            <td class="status-${order.status}">${order.status}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/order/track?id=${order.id}"
                                   class="btn btn-small">
                                    Отследить
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>
    </main>

    <%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
</body>
</html>

