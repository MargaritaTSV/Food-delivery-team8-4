<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Кабинет магазина - Food Delivery</title>
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

<main class="container grid-2">
    <section class="card">
        <div class="section-header">
            <div>
                <h2>Статус магазина</h2>
                <p class="muted">ACTIVE, SUSPENDED, CLOSED</p>
            </div>
            <span class="status-badge">${shop.status}</span>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/shop/status" class="stacked-form">
            <label>Изменить статус</label>
            <select name="status" required>
                <option value="ACTIVE">ACTIVE</option>
                <option value="SUSPENDED">SUSPENDED</option>
                <option value="CLOSED">CLOSED</option>
            </select>
            <button type="submit" class="btn btn-primary">Сохранить</button>
        </form>
    </section>

    <section class="card">
        <h2>Активация аккаунта</h2>
        <p class="muted">После модерации отправляем ссылку на emailForAU. Перейдите по ней, чтобы получить статус ACTIVE и shopId.</p>
        <form method="POST" action="${pageContext.request.contextPath}/shop/resend-confirmation" class="stacked-form">
            <label>Email для подтверждения</label>
            <input type="email" name="emailForAU" value="${shop.emailForAU}" required>
            <button type="submit" class="btn btn-secondary">Выслать ссылку повторно</button>
        </form>
    </section>

    <section class="card">
        <h2>Публичная информация</h2>
        <form method="POST" action="${pageContext.request.contextPath}/shop/update-public" class="stacked-form">
            <label>Публичный телефон</label>
            <input type="tel" name="publicNumber" value="${shop.publicNumber}" required>

            <label>Публичная почта</label>
            <input type="email" name="publicEmail" value="${shop.contactEmail}" required>

            <label>Описание</label>
            <textarea name="description" rows="3">${shop.description}</textarea>

            <label>Тип/категория</label>
            <input type="text" name="type" value="${shop.storeType}">

            <label>Витринное название</label>
            <input type="text" name="naming" value="${shop.naming}">

            <button type="submit" class="btn btn-primary">Обновить данные</button>
        </form>
    </section>

    <section class="card">
        <h2>Безопасность аккаунта</h2>
        <form method="POST" action="${pageContext.request.contextPath}/shop/update-contacts" class="stacked-form">
            <p class="muted">Смена emailForAU / phoneForAU потребует подтверждения кода.</p>
            <label>Новый email для входа</label>
            <input type="email" name="emailForAU" placeholder="example@gmail.com">
            <label>Новый телефон для входа</label>
            <input type="tel" name="phoneForAU" placeholder="89XXXXXXXXX">
            <label>Пароль для подтверждения</label>
            <input type="password" name="password" required>
            <label>Код из почты/телефона</label>
            <input type="text" name="otp" placeholder="6 цифр">
            <button type="submit" class="btn btn-secondary">Сохранить контакты</button>
        </form>

        <form method="POST" action="${pageContext.request.contextPath}/shop/update-password" class="stacked-form">
            <label>Старый пароль</label>
            <input type="password" name="oldPassword" required>
            <label>Новый пароль</label>
            <input type="password" name="newPassword" minlength="8" placeholder="Спецзнак, цифра, заглавная" required>
            <label>Подтверждение кода</label>
            <input type="text" name="code" placeholder="Из email/телефона" required>
            <button type="submit" class="btn btn-primary">Сменить пароль</button>
        </form>
    </section>

    <section class="card wide">
        <h2>Адрес магазина</h2>
        <form method="POST" action="${pageContext.request.contextPath}/shop/update-address" class="form-grid">
            <div class="form-group">
                <label>Страна</label>
                <input type="text" name="country" value="${shop.country}" required>
            </div>
            <div class="form-group">
                <label>Город</label>
                <input type="text" name="city" value="${shop.city}" required>
            </div>
            <div class="form-group">
                <label>Улица</label>
                <input type="text" name="street" value="${shop.street}" required>
            </div>
            <div class="form-group">
                <label>Дом</label>
                <input type="text" name="building" value="${shop.building}" required>
            </div>
            <div class="form-group">
                <label>Квартира/офис</label>
                <input type="text" name="apartment" value="${shop.apartment}">
            </div>
            <div class="form-group">
                <label>Подъезд</label>
                <input type="text" name="entrance" value="${shop.entrance}">
            </div>
            <div class="form-group">
                <label>Этаж</label>
                <input type="text" name="floor" value="${shop.floor}">
            </div>
            <div class="form-group">
                <label>Район</label>
                <input type="text" name="district" value="${shop.district}">
            </div>
            <div class="form-group">
                <label>Координаты (широта)</label>
                <input type="text" name="latitude" value="${shop.latitude}">
            </div>
            <div class="form-group">
                <label>Координаты (долгота)</label>
                <input type="text" name="longitude" value="${shop.longitude}">
            </div>
            <div class="form-group full">
                <label>Комментарий к адресу</label>
                <textarea name="addressNote" rows="2">${shop.addressNote}</textarea>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить адрес</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
