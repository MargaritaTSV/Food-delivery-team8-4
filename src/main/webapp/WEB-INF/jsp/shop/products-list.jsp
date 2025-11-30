<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Товары - Food Delivery</title>
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
                <h1>Управление товарами</h1>
                <p class="muted">Добавляйте блюда, меняйте цены, доступность и карточки без JavaScript.</p>
            </div>
            <a href="${pageContext.request.contextPath}/products/add-form" class="btn btn-primary">+ Добавить товар</a>
        </div>

        <c:if test="${not empty param.added}">
            <div class="alert alert-success">Товар добавлен!</div>
        </c:if>
        <c:if test="${not empty param.updated}">
            <div class="alert alert-success">Товар обновлён!</div>
        </c:if>
        <c:if test="${not empty param.deleted}">
            <div class="alert alert-success">Товар удалён!</div>
        </c:if>

        <table class="products-table">
            <thead>
                <tr>
                    <th>Название</th>
                    <th>Цена</th>
                    <th>Категория</th>
                    <th>Вес</th>
                    <th>Время готовки</th>
                    <th>Доступность</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="product" items="${products}">
                    <tr>
                        <td>
                            <div class="product-name">${product.name}</div>
                            <div class="muted small">${product.description}</div>
                        </td>
                        <td>${product.price} ₽</td>
                        <td>${product.category}</td>
                        <td>${product.weight} г</td>
                        <td>${product.cookingTimeMinutes} мин</td>
                        <td>
                            <form method="POST" action="${pageContext.request.contextPath}/products/toggle-availability" class="inline-form">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <input type="hidden" name="available" value="${product.available}">
                                <button type="submit" class="btn ${product.available ? 'btn-success' : 'btn-danger'} btn-small">
                                    ${product.available ? 'Доступен' : 'Недоступен'}
                                </button>
                            </form>
                        </td>
                        <td class="actions">
                            <a href="${pageContext.request.contextPath}/products/edit-form?id=${product.productId}" class="btn btn-secondary btn-small">Редактировать</a>
                            <form method="POST" action="${pageContext.request.contextPath}/products/delete" class="inline-form">
                                <input type="hidden" name="productId" value="${product.productId}">
                                <button type="submit" class="btn btn-danger btn-small">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </main>
</body>
</html>
