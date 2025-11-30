<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Оформление заказа - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/navbar.jsp" %>

    <main class="container">
        <h1>Оформление заказа</h1>

        <c:if test="${empty cartItems}">
            <p class="empty-cart">Корзина пуста. Добавьте товары, чтобы оформить заказ.</p>
            <a href="${pageContext.request.contextPath}/client/home" class="btn btn-primary">Вернуться к покупкам</a>
        </c:if>

        <c:if test="${not empty cartItems}">
            <section class="section">
                <h2>Состав заказа</h2>
                <table class="cart-table">
                    <thead>
                        <tr>
                            <th>Товар</th>
                            <th>Цена</th>
                            <th>Количество</th>
                            <th>Итого</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${cartItems}">
                            <tr>
                                <td>${item.productName}</td>
                                <td>${item.price} ₽</td>
                                <td>${item.quantity}</td>
                                <td>${item.price * item.quantity} ₽</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <p><strong>Сумма к оплате:</strong> ${total} ₽</p>
            </section>

            <section class="section">
                <h2>Адрес доставки</h2>
                <form method="POST" action="${pageContext.request.contextPath}/cart/checkout">
                    <input type="hidden" name="cartId" value="${cart.id}">
                    <div class="info-grid">
                        <div class="form-group">
                            <label>Страна:</label>
                            <input type="text" name="country" value="${client.address.country}" required>
                        </div>
                        <div class="form-group">
                            <label>Город:</label>
                            <input type="text" name="city" value="${client.address.city}" required>
                        </div>
                        <div class="form-group">
                            <label>Улица:</label>
                            <input type="text" name="street" value="${client.address.street}" required>
                        </div>
                        <div class="form-group">
                            <label>Здание:</label>
                            <input type="text" name="building" value="${client.address.building}" required>
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

                    <h3 style="margin-top: 20px;">Оплата</h3>
                    <div class="form-group">
                        <label class="sr-only">Способ оплаты</label>
                        <div class="info-grid">
                            <label><input type="radio" name="paymentMethod" value="CARD" checked> Карта курьеру</label>
                            <label><input type="radio" name="paymentMethod" value="CASH"> Наличные</label>
                            <label><input type="radio" name="paymentMethod" value="ONLINE"> Онлайн</label>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-success btn-large">Подтвердить заказ</button>
                </form>
            </section>
        </c:if>
    </main>

    <%@ include file="/WEB-INF/jsp/layout/footer.jsp" %>
</body>
</html>
