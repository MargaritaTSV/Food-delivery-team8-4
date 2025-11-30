<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация магазина - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-box">
            <h1>Food Delivery</h1>
            <h2>Регистрация магазина/ресторана</h2>

            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form method="POST" action="${pageContext.request.contextPath}/shop/register" class="stacked-form">
                <div class="form-grid">
                    <div class="form-group">
                        <label>Название (витрина)</label>
                        <input type="text" name="naming" placeholder="Например, Bella Pizza" required>
                    </div>
                    <div class="form-group">
                        <label>Официальное название</label>
                        <input type="text" name="offName" placeholder="ООО &quot;Белла Пицца&quot;" required>
                    </div>
                    <div class="form-group">
                        <label>Тип магазина</label>
                        <input type="text" name="storeType" placeholder="Пицца, суши, бургерная" required>
                    </div>
                    <div class="form-group">
                        <label>Ценовой диапазон</label>
                        <select name="priceRange" required>
                            <option value="">-- выбрать --</option>
                            <option value="LOW">LOW</option>
                            <option value="MEDIUM">MEDIUM</option>
                            <option value="HIGH">HIGH</option>
                            <option value="PREMIUM">PREMIUM</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label>Описание</label>
                    <textarea name="description" rows="3" placeholder="Кратко о кухне, особенностях и доставке"></textarea>
                </div>

                <h3>Контакты для аутентификации</h3>
                <div class="form-grid">
                    <div class="form-group">
                        <label>Email для входа</label>
                        <input type="email" name="emailForAU" placeholder="example@gmail.com" required>
                    </div>
                    <div class="form-group">
                        <label>Телефон для входа</label>
                        <input type="tel" name="privateNumber" placeholder="89XXXXXXXXX" required>
                    </div>
                    <div class="form-group">
                        <label>Пароль</label>
                        <input type="password" name="password" minlength="8" placeholder="Минимум 8 символов, спецзнак, цифра, заглавная" required>
                    </div>
                </div>

                <h3>Публичные контакты</h3>
                <div class="form-grid">
                    <div class="form-group">
                        <label>Публичный телефон</label>
                        <input type="tel" name="publicNumber" placeholder="89XXXXXXXXX" required>
                    </div>
                    <div class="form-group">
                        <label>Публичная почта</label>
                        <input type="email" name="contactEmail" placeholder="info@shop.com" required>
                    </div>
                    <div class="form-group">
                        <label>Сайт</label>
                        <input type="url" name="website" placeholder="https://shop.com">
                    </div>
                    <div class="form-group">
                        <label>Район</label>
                        <input type="text" name="district" placeholder="Центральный" required>
                    </div>
                </div>

                <h3>Адрес магазина/ресторана</h3>
                <div class="form-grid">
                    <div class="form-group">
                        <label>Страна</label>
                        <input type="text" name="country" required>
                    </div>
                    <div class="form-group">
                        <label>Город</label>
                        <input type="text" name="city" required>
                    </div>
                    <div class="form-group">
                        <label>Улица</label>
                        <input type="text" name="street" required>
                    </div>
                    <div class="form-group">
                        <label>Дом</label>
                        <input type="text" name="building" required>
                    </div>
                    <div class="form-group">
                        <label>Квартира/офис</label>
                        <input type="text" name="apartment">
                    </div>
                    <div class="form-group">
                        <label>Подъезд</label>
                        <input type="text" name="entrance">
                    </div>
                    <div class="form-group">
                        <label>Этаж</label>
                        <input type="text" name="floor">
                    </div>
                    <div class="form-group">
                        <label>Координаты (широта)</label>
                        <input type="text" name="latitude" placeholder="55.751244">
                    </div>
                    <div class="form-group">
                        <label>Координаты (долгота)</label>
                        <input type="text" name="longitude" placeholder="37.618423">
                    </div>
                </div>
                <div class="form-group">
                    <label>Комментарий к адресу</label>
                    <textarea name="addressNote" rows="2" placeholder="Как пройти к кухне, код домофона"></textarea>
                </div>

                <h3>Данные владельца</h3>
                <div class="form-grid">
                    <div class="form-group">
                        <label>Фамилия</label>
                        <input type="text" name="ownerLastName" required>
                    </div>
                    <div class="form-group">
                        <label>Имя</label>
                        <input type="text" name="ownerFirstName" required>
                    </div>
                    <div class="form-group">
                        <label>Отчество</label>
                        <input type="text" name="ownerMiddleName">
                    </div>
                    <div class="form-group">
                        <label>Телефон владельца</label>
                        <input type="tel" name="ownerPhoneNumber" placeholder="89XXXXXXXXX" required>
                    </div>
                    <div class="form-group">
                        <label>Email владельца</label>
                        <input type="email" name="ownerEmail" placeholder="owner@gmail.com" required>
                    </div>
                    <div class="form-group">
                        <label>Должность</label>
                        <input type="text" name="ownerPosition" placeholder="Владелец/Менеджер" required>
                    </div>
                </div>

                <div class="alert alert-info">После отправки анкеты магазин получает статус <strong>PENDING</strong>. Мы отправим ссылку для подтверждения на указанный email и телефон.</div>

                <button type="submit" class="btn btn-primary btn-block">Отправить заявку</button>
            </form>

            <p class="auth-link">Уже есть аккаунт? <a href="${pageContext.request.contextPath}/shop/login">Войти</a></p>
        </div>
    </div>
</body>
</html>
