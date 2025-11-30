<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${requestScope.isEdit ? 'Редактировать товар' : 'Добавить товар'} - Food Delivery</title>
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
        <h1>${requestScope.isEdit ? 'Редактировать товар' : 'Добавить товар'}</h1>

        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <form method="POST" action="${requestScope.isEdit ? pageContext.request.contextPath.concat('/products/update') : pageContext.request.contextPath.concat('/products/add')}" class="product-form">
            <c:if test="${requestScope.isEdit}">
                <input type="hidden" name="productId" value="${product.productId}">
            </c:if>

            <div class="form-grid">
                <div class="form-group">
                    <label>Название</label>
                    <input type="text" name="name" value="${product.name}" required>
                </div>
                <div class="form-group">
                    <label>Цена</label>
                    <input type="number" name="price" value="${product.price}" step="0.01" required>
                </div>
                <div class="form-group">
                    <label>Категория</label>
                    <select name="category" required>
                        <option value="">-- Выберите --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}" ${product.category == cat ? 'selected' : ''}>${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Вес, г</label>
                    <input type="number" name="weight" value="${product.weight}" min="0" step="1" placeholder="Например, 350" required>
                </div>
                <div class="form-group">
                    <label>Время готовки (мин)</label>
                    <input type="number" name="cookingTimeMinutes" value="${product.cookingTimeMinutes}" min="0" step="1">
                </div>
            </div>

            <div class="form-group">
                <label>Краткое описание</label>
                <textarea name="descriptionOfProduct" rows="3" placeholder="Состав, вкус и особенности">${product.descriptionOfProduct}${product.description}</textarea>
            </div>
            <div class="form-group">
                <label>Ингредиенты (consistsOf)</label>
                <textarea name="consistsOf" rows="2" placeholder="Тесто, сыр моцарелла, томатный соус">${product.consistsOf}</textarea>
            </div>
            <div class="form-group">
                <label>Аллергены</label>
                <input type="text" name="allergens" value="${product.allergens}" placeholder="Молоко, глютен">
            </div>
            <div class="form-group">
                <label>Ссылка на изображение</label>
                <input type="url" name="imageUrl" value="${product.imageUrl}" placeholder="https://...">
            </div>

            <div class="form-group">
                <label class="checkbox-inline">
                    <input type="checkbox" name="isAvailable" ${product.available ? 'checked' : ''}>
                    Товар доступен к заказу
                </label>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">${requestScope.isEdit ? 'Обновить' : 'Добавить'}</button>
                <a href="${pageContext.request.contextPath}/products/list" class="btn btn-secondary">Отмена</a>
            </div>
        </form>
    </main>
</body>
</html>
