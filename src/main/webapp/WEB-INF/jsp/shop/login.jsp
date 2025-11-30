<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Вход магазина - Food Delivery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-box">
            <h1>Food Delivery</h1>
            <h2>Вход в кабинет магазина</h2>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>
            <c:if test="${not empty notice}">
                <div class="alert alert-info">${notice}</div>
            </c:if>

            <form method="POST" action="${pageContext.request.contextPath}/shop/login" class="stacked-form">
                <div class="form-group">
                    <label>Email или телефон для аутентификации</label>
                    <input type="text" name="login" placeholder="example@gmail.com или 89XXXXXXXXX" required>
                </div>

                <div class="form-group">
                    <label>Пароль</label>
                    <input type="password" name="password" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Войти</button>
            </form>

            <div class="auth-hint">
                <p>Только активированные магазины получают доступ к панели. После модерации мы отправим ссылку на email/телефон из анкеты.</p>
            </div>

            <p class="auth-link">Новый магазин? <a href="${pageContext.request.contextPath}/shop/register">Зарегистрировать</a></p>
        </div>
    </div>
</body>
</html>
